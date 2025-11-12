package com.jxdx.classroom.activity;

import android.media.projection.MediaProjection;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 屏幕推流管理类，负责RTMP连接、视频数据发送和资源管理
 * 已修复：与Native层方法名对齐、编码器线程安全、异常防护
 */
public class ScreenLive extends Thread {
    private static final String TAG = "ScreenLive";
    private static final int QUEUE_CAPACITY = 50; // 队列容量限制，避免OOM
    private String rtmpUrl;
    private MediaProjection mediaProjection;
    // 带容量限制的阻塞队列，防止视频包堆积导致内存溢出
    private final LinkedBlockingQueue<RTMPPackage> packetQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicBoolean isLiving = new AtomicBoolean(false); // 线程安全的推流状态
    private final AtomicBoolean isConnected = new AtomicBoolean(false); // RTMP连接状态
    private VideoCodec videoCodec; // 编码器引用，用于释放

    // 加载Native库（确保libnative-lib.so已正确编译）
    static {
        try {
            System.loadLibrary("native-lib");
            Log.i(TAG, "Native库加载成功");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native库加载失败！请检查.so文件是否存在或架构匹配", e);
            throw e; // 加载失败直接抛出，避免后续空指针
        }
    }

    /**
     * 开始推流（外部调用入口）
     * @param url RTMP服务器地址（如 rtmp://xxx.xxx.xxx.xxx/live/stream）
     * @param projection 媒体投影实例（需从MediaProjectionManager获取）
     */
    public void startLive(String url, MediaProjection projection) {
        // 1. 前置状态检查：避免重复启动
        if (isLiving.get()) {
            Log.w(TAG, "推流已在运行中，无需重复调用startLive");
            return;
        }
        // 2. 参数合法性校验
        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "startLive失败：RTMP地址为空");
            return;
        }
        if (projection == null) {
            Log.e(TAG, "startLive失败：MediaProjection为空（需先获取屏幕共享权限）");
            return;
        }
        // 3. 赋值并启动推流线程
        this.rtmpUrl = url.trim();
        this.mediaProjection = projection;
        this.start(); // 启动run()方法中的逻辑
        Log.i(TAG, "startLive：推流线程已启动，准备连接RTMP服务器");
    }

    /**
     * 停止推流（外部调用入口，线程安全）
     */
    public void stopLive() {
        // 1. 状态检查：避免重复停止
        if (!isLiving.get()) {
            Log.w(TAG, "推流未运行，无需调用stopLive");
            return;
        }
        // 2. 标记状态为停止，中断阻塞队列
        isLiving.set(false);
        interrupt(); // 中断packetQueue.take()的阻塞
        Log.i(TAG, "stopLive：已触发推流停止流程");
    }

    @Override
    public void run() {
        Log.i(TAG, "推流线程启动，RTMP地址：" + rtmpUrl);
        try {
            // 步骤1：建立RTMP连接（调用Native层connect方法）
            Log.i(TAG, "run：开始建立RTMP连接，URL: " + rtmpUrl);
            long connectStartTime = System.currentTimeMillis();
            boolean connectResult = connect(rtmpUrl);
            long connectDuration = System.currentTimeMillis() - connectStartTime;
            
            if (!connectResult) {
                Log.e(TAG, "run：RTMP连接失败（耗时: " + connectDuration + "ms），检查服务器地址、网络或Native层逻辑");
                return;
            }
            isConnected.set(true); // 标记连接成功
            Log.i(TAG, "run：RTMP连接成功（耗时: " + connectDuration + "ms），准备初始化编码器");

            // 步骤2：初始化视频编码器（ScreenLive与VideoCodec解耦，通过addPacket通信）
            videoCodec = new VideoCodec(this);
            if (!videoCodec.startEncoding(mediaProjection)) {
                Log.e(TAG, "run：视频编码器启动失败");
                return;
            }

            // 步骤3：进入数据发送循环（核心逻辑）
            isLiving.set(true);
            Log.i(TAG, "run：推流正式开始，进入数据发送循环");
            int packetCount = 0;
            while (isLiving.get()) {
                try {
                    // 阻塞获取视频包（队列空时会等待，可被interrupt中断）
                    RTMPPackage packet = packetQueue.take();
                    packetCount++;
                    Log.d(TAG, "run：收到第" + packetCount + "个视频包，大小: " + packet.getBuffer().length + 
                          ", 时间戳: " + packet.getTms());
                    
                    // 校验视频包有效性（双重保险，避免无效数据传入Native）
                    if (isValidPacket(packet)) {
                        // 调用Native层sendData方法（关键：与Native层方法名对齐）
                        long sendStartTime = System.currentTimeMillis();
                        boolean sendSuccess = sendData(
                                packet.getBuffer(),
                                packet.getBuffer().length,
                                packet.getTms()
                        );
                        long sendDuration = System.currentTimeMillis() - sendStartTime;
                        
                        if (!sendSuccess) {
                            Log.w(TAG, "run：Native层发送数据失败（耗时: " + sendDuration + "ms），可能是RTMP连接断开");
                            // 连接断开时主动停止推流
                            isLiving.set(false);
                        } else {
                            Log.d(TAG, "run：第" + packetCount + "个视频包发送成功（耗时: " + sendDuration + "ms）");
                        }
                    } else {
                        Log.w(TAG, "run：忽略无效视频包（buffer空或长度0）");
                    }
                } catch (InterruptedException e) {
                    // 捕获中断异常（stopLive调用时触发），正常退出循环
                    Log.i(TAG, "run：推流线程被中断，准备退出循环");
                    break;
                } catch (Exception e) {
                    // 捕获其他异常，避免线程意外崩溃
                    Log.e(TAG, "run：数据发送循环异常", e);
                    // 异常时主动停止推流
                    isLiving.set(false);
                }
            }
        } catch (Exception e) {
            // 捕获run()方法中所有未处理的异常，确保线程不崩溃
            Log.e(TAG, "run：推流线程整体异常", e);
        } finally {
            // 无论成功/失败，最终都释放资源
            releaseResources();
            Log.i(TAG, "run：推流线程已退出，资源已释放");
        }
    }

    /**
     * 向推流队列添加视频包（供VideoCodec调用，线程安全）
     * @param packet 编码后的H.264视频包（需包含完整的起始码00 00 00 01）
     */
    public void addPacket(RTMPPackage packet) {
        // 1. 状态检查：推流停止则拒绝添加
        if (!isLiving.get() || !isConnected.get()) {
            Log.w(TAG, "addPacket：推流已停止或未连接，拒绝添加视频包");
            return;
        }
        // 2. 包有效性校验
        if (!isValidPacket(packet)) {
            Log.w(TAG, "addPacket：无效视频包，拒绝添加");
            return;
        }
        // 3. 添加到队列（队列满时阻塞，避免OOM）
        try {
            packetQueue.put(packet);
            // 可选：打印队列状态，便于调试
            // Log.d(TAG, "addPacket：视频包已加入队列，当前队列大小：" + packetQueue.size());
        } catch (InterruptedException e) {
            Log.w(TAG, "addPacket：添加视频包时被中断（推流已停止）", e);
        }
    }

    /**
     * 校验RTMPPackage是否有效（内部工具方法）
     */
    private boolean isValidPacket(RTMPPackage packet) {
        return packet != null
                && packet.getBuffer() != null
                && packet.getBuffer().length > 4 // H.264帧至少包含4字节起始码（00 00 00 01）
                && packet.getTms() >= 0; // 时间戳不能为负数
    }

    /**
     * 释放所有资源（线程安全，内部调用）
     */
    private void releaseResources() {
        Log.i(TAG, "releaseResources：开始释放推流资源");
        // 1. 重置状态标记
        isLiving.set(false);
        isConnected.set(false);

        // 2. 停止并释放编码器（需VideoCodec实现stopEncoding方法）
        if (videoCodec != null) {
            try {
                videoCodec.stopEncoding();
                Log.i(TAG, "releaseResources：编码器已停止");
            } catch (Exception e) {
                Log.e(TAG, "releaseResources：释放编码器异常", e);
            } finally {
                videoCodec = null;
            }
        }

        // 3. 释放MediaProjection（避免屏幕共享权限泄漏）
        if (mediaProjection != null) {
            try {
                mediaProjection.stop();
                Log.i(TAG, "releaseResources：MediaProjection已停止");
            } catch (Exception e) {
                Log.e(TAG, "releaseResources：释放MediaProjection异常", e);
            } finally {
                mediaProjection = null;
            }
        }

        // 4. 通知Native层释放资源（RTMP连接、SPS/PPS内存等）
        try {
            close();
            Log.i(TAG, "releaseResources：Native层资源已释放");
        } catch (Exception e) {
            Log.e(TAG, "releaseResources：调用Native层close异常", e);
        }

        // 5. 清空队列（避免残留视频包占用内存）
        packetQueue.clear();
        Log.i(TAG, "releaseResources：推流队列已清空");

        // 6. 重置其他变量
        rtmpUrl = null;
        Log.i(TAG, "releaseResources：所有资源释放完成");
    }

    // -------------------------- Native方法声明（与native-lib.cpp完全对齐）--------------------------
    /**
     * 建立RTMP连接（调用Native层Java_com_jxdx_classroom_activity_ScreenLive_connect）
     * @param url RTMP服务器地址
     * @return true：连接成功；false：连接失败
     */
    private native boolean connect(String url);

    /**
     * 发送视频数据到RTMP服务器（调用Native层Java_com_jxdx_classroom_activity_ScreenLive_sendData）
     * 关键：方法名改为sendData，与Native层实现完全匹配（之前的nativeSendData是错误的）
     * @param data 编码后的H.264视频数据（包含00 00 00 01起始码）
     * @param len 数据长度
     * @param tms 时间戳（毫秒）
     * @return true：发送成功；false：发送失败
     */
    private native boolean sendData(byte[] data, int len, long tms);

    /**
     * 释放Native层资源（调用Native层Java_com_jxdx_classroom_activity_ScreenLive_close）
     * 包括：RTMP连接关闭、SPS/PPS内存释放、Live结构体释放等
     */
    private native void close();

    // -------------------------- RTMP数据包封装类（内部使用，避免外部依赖）--------------------------
    /**
     * RTMP视频数据包封装（存储H.264数据和时间戳）
     */
    public static class RTMPPackage {
        private final byte[] buffer; // H.264视频数据（必须包含00 00 00 01起始码）
        private final long tms;      // 时间戳（毫秒，用于RTMP时序同步）

        /**
         * 构造RTMP数据包
         * @param buffer H.264视频数据（非空，长度>4）
         * @param tms 时间戳（>=0）
         */
        public RTMPPackage(byte[] buffer, long tms) {
            this.buffer = buffer;
            this.tms = tms;
        }

        // Getter方法（仅提供读取，避免外部修改）
        public byte[] getBuffer() {
            return buffer;
        }

        public long getTms() {
            return tms;
        }
    }
}