package com.jxdx.classroom.activity;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 视频编码器，负责屏幕捕获和H.264编码
 */
public class VideoCodec {
    private static final String TAG = "VideoCodec";
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final int WIDTH = 1280;    // 视频宽度
    private static final int HEIGHT = 720;    // 视频高度
    private static final int BIT_RATE = 2_000_000; // 码率（2Mbps）
    private static final int FRAME_RATE = 30; // 帧率
    private static final int I_FRAME_INTERVAL = 2; // 关键帧间隔（秒）

    private final ScreenLive screenLive;
    private final AtomicBoolean isEncoding = new AtomicBoolean(false);
    private MediaCodec mediaCodec;
    private MediaProjection mediaProjection;
    private Surface inputSurface; // 编码器输入表面
    private Thread codecProcessingThread; // 编码线程引用，用于同步

    public VideoCodec(ScreenLive screenLive) {
        this.screenLive = screenLive;
    }

    /**
     * 开始编码
     * @param projection 媒体投影实例
     * @return true：启动成功
     */
    public boolean startEncoding(MediaProjection projection) {
        if (isEncoding.get()) {
            Log.w(TAG, "编码器已在运行中");
            return true;
        }

        this.mediaProjection = projection;
        try {
            // 1. 初始化编码器
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = mediaCodec.createInputSurface();
            mediaCodec.start();

            // 2. 启动屏幕捕获（将屏幕内容绘制到编码器的输入表面）
            // 使用唯一名称创建虚拟显示，避免SurfaceFlinger重复图层名称警告
            String uniqueDisplayName = "ScreenCapture_" + System.currentTimeMillis();
            projection.createVirtualDisplay(
                    uniqueDisplayName,
                    WIDTH, HEIGHT, 1, // 屏幕密度（1表示默认）
                    0, // 显示标志
                    inputSurface,
                    null, // 显示回调
                    null  // 回调 Handler（使用当前线程的Looper）
            );
            Log.i(TAG, "已创建虚拟显示：" + uniqueDisplayName);

            isEncoding.set(true);
            // 3. 启动编码数据处理线程
            startCodecProcessingThread();
            Log.i(TAG, "视频编码器启动成功");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "视频编码器启动失败", e);
            stopEncoding();
            return false;
        }
    }

    /**
     * 启动编码数据处理线程
     */
    private void startCodecProcessingThread() {
        codecProcessingThread = new Thread(() -> {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (isEncoding.get()) {
                try {
                    // 双重检查mediaCodec是否为null，避免资源释放后访问
                    if (mediaCodec == null) {
                        Log.w(TAG, "编码线程检测到mediaCodec已释放，准备退出");
                        break;
                    }
                    
                    // 获取编码后的视频数据
                    int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputBufferId >= 0) {
                        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            // 复制数据到新的字节数组（避免ByteBuffer生命周期问题）
                            byte[] data = new byte[bufferInfo.size];
                            outputBuffer.get(data);
                            outputBuffer.clear();

                            // 发送到RTMP推流器
                            // presentationTimeUs 是微秒，转换为毫秒
                            long timestamp = bufferInfo.presentationTimeUs / 1000;
                            Log.d(TAG, "发送编码数据: 大小=" + data.length + ", 时间戳=" + timestamp + 
                                  ", 标志=" + bufferInfo.flags);
                            screenLive.addPacket(
                                    new ScreenLive.RTMPPackage(data, timestamp)
                            );
                        }
                        // 释放输出缓冲区
                        mediaCodec.releaseOutputBuffer(outputBufferId, false);
                    }

                    // 检查是否编码结束
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.i(TAG, "编码结束");
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "编码数据处理异常", e);
                    break;
                }
            }
            Log.i(TAG, "编码数据处理线程已退出");
        }, "VideoCodec-Processor");
        codecProcessingThread.start();
    }

    /**
     * 停止编码并释放资源
     */
    public void stopEncoding() {
        if (!isEncoding.get()) {
            Log.w(TAG, "编码器未运行，无需停止");
            return;
        }

        Log.i(TAG, "停止视频编码器");
        // 1. 首先设置状态标记为停止
        isEncoding.set(false);

        // 2. 等待编码线程退出（最多等待2000ms）
        if (codecProcessingThread != null && codecProcessingThread.isAlive()) {
            try {
                Log.i(TAG, "等待编码线程退出...");
                codecProcessingThread.join(2000); // 设置超时，避免永久阻塞
                Log.i(TAG, "编码线程已退出");
            } catch (InterruptedException e) {
                Log.e(TAG, "等待编码线程退出被中断", e);
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
            codecProcessingThread = null;
        }

        // 3. 停止并释放编码器
        if (mediaCodec != null) {
            try {
                mediaCodec.stop();
                mediaCodec.release();
                Log.i(TAG, "编码器资源已释放");
            } catch (Exception e) {
                Log.e(TAG, "释放编码器失败", e);
            } finally {
                mediaCodec = null;
            }
        }

        // 4. 释放输入表面
        if (inputSurface != null) {
            inputSurface.release();
            Log.i(TAG, "输入表面已释放");
            inputSurface = null;
        }

        // 5. 释放媒体投影（由ScreenLive统一管理，这里仅置空引用）
        mediaProjection = null;
    }
}
