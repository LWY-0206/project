package com.jxdx.classroom.activity;

 
 import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;

import java.util.concurrent.atomic.AtomicBoolean;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;

/**
 * 使用FFmpegKit实现的屏幕推流管理类
 * 提供与原有ScreenLive相同的接口，但使用FFmpeg处理推流
 * 
 * 使用示例：
 * 1. 创建实例
 * FFmpegScreenLive ffmpegLive = new FFmpegScreenLive();
 * 
 * 2. 设置自定义参数（可选）
 * ffmpegLive.setStreamParameters(1280, 720, 30, 2000000);
 * 
 * 3. 开始推流
 * boolean started = ffmpegLive.startLive("rtmp://your-server/live/stream", mediaProjection);
 * 
 * 4. 检查推流状态
 * boolean isStreaming = ffmpegLive.isStreaming();
 * 
 * 5. 停止推流
 * ffmpegLive.stopLive();
 */
public class FFmpegScreenLive {
    private static final String TAG = "FFmpegScreenLive";
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private String rtmpUrl;
    private MediaProjection mediaProjection;
    private FFmpegSession ffmpegSession;
    
    // 屏幕编码相关
    private MediaCodec videoEncoder;
    private Surface inputSurface;
    private VirtualDisplay virtualDisplay;
    private Thread drainThread;
    private volatile boolean drainRunning = false;
    private Socket h264Socket;
    private OutputStream h264Output;
    private int localH264Port = 12721;
    
    // 推流参数
    private int width = 1280;
    private int height = 720;
    private int frameRate = 30;
    private int bitrate = 2000000; // 2Mbps

    /**
     * 开始推流
     * @param url RTMP服务器地址
     * @param projection MediaProjection实例
     * @return true：启动成功；false：启动失败
     */
    public boolean startLive(String url, MediaProjection projection) {
        // 1. 参数校验
        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "startLive失败：RTMP地址为空");
            return false;
        }
        if (projection == null) {
            Log.e(TAG, "startLive失败：MediaProjection为空");
            return false;
        }
        if (isStreaming.get()) {
            Log.w(TAG, "startLive：推流已在运行中");
            return false;
        }

        this.rtmpUrl = url.trim();
        this.mediaProjection = projection;
        isStreaming.set(true);

        // 2. 启动推流线程
        new Thread(() -> {
            try {
                // 启动屏幕捕获准备
                startScreenCapture();
                
                // 执行FFmpeg命令
                executeFFmpegCommand();
            } catch (Exception e) {
                Log.e(TAG, "FFmpeg推流过程中异常", e);
                releaseResources();
            }
        }, "FFmpeg-ScreenLive").start();

        Log.i(TAG, "startLive：FFmpeg推流已启动，目标地址：" + rtmpUrl);
        return true;
    }

    /**
     * 停止推流
     */
    public void stopLive() {
        if (!isStreaming.get()) {
            Log.w(TAG, "stopLive：推流未运行");
            return;
        }

        Log.i(TAG, "stopLive：开始停止推流");
        releaseResources();
    }

    /**
     * 启动屏幕捕获准备
     */
    private void startScreenCapture() {
        try {
            Log.i(TAG, "startScreenCapture：初始化 MediaCodec H.264 编码");
            
            // 1. 创建 H.264 编码器
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            videoEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = videoEncoder.createInputSurface();
            videoEncoder.start();

            // 2. 注册 MediaProjection 回调（Android 6.0+ 必需）
            mediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    Log.i(TAG, "MediaProjection 已停止");
                    releaseScreenCaptureResources();
                }
            }, new Handler(Looper.getMainLooper()));

            // 3. 创建 VirtualDisplay 采集屏幕
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "FFmpegScreen",
                    width,
                    height,
                    1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    inputSurface,
                    null,
                    null
            );

            // 3. 启动编码输出线程
            drainRunning = true;
            drainThread = new Thread(this::drainEncoderLoop, "H264-Drain");
            drainThread.start();
            
            Log.i(TAG, "startScreenCapture：屏幕采集与编码已启动");
        } catch (Exception e) {
            Log.e(TAG, "startScreenCapture：屏幕捕获准备异常", e);
            // 清理已创建的资源
            releaseScreenCaptureResources();
            throw new RuntimeException("屏幕采集初始化失败", e);
        }
    }

    /**
     * 释放屏幕采集相关资源
     */
    private void releaseScreenCaptureResources() {
        try {
            drainRunning = false;
            if (drainThread != null) {
                drainThread.interrupt();
                try { drainThread.join(500); } catch (InterruptedException ignored) {}
                drainThread = null;
            }
            closeSocketQuietly();

            if (virtualDisplay != null) {
                try { virtualDisplay.release(); } catch (Exception ignored) {}
                virtualDisplay = null;
            }
            if (inputSurface != null) {
                try { inputSurface.release(); } catch (Exception ignored) {}
                inputSurface = null;
            }
            if (videoEncoder != null) {
                try { videoEncoder.stop(); } catch (Exception ignored) {}
                try { videoEncoder.release(); } catch (Exception ignored) {}
                videoEncoder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "释放屏幕采集资源异常", e);
        }
    }

    /**
     * 编码器输出循环，将 H.264 数据写入本地 TCP
     */
    private void drainEncoderLoop() {
        try {
            // 等待 FFmpeg 开始监听本地端口
            int retries = 0;
            while (drainRunning && (h264Socket == null || !h264Socket.isConnected())) {
                try {
                    h264Socket = new Socket();
                    h264Socket.connect(new InetSocketAddress("127.0.0.1", localH264Port), 300);
                    h264Output = h264Socket.getOutputStream();
                    Log.i(TAG, "已连接本地H264写入端口:" + localH264Port);
                    break;
                } catch (IOException e) {
                    closeSocketQuietly();
                    if (++retries > 50) {
                        Log.e(TAG, "连接本地H264端口失败次数过多，放弃: " + e.getMessage());
                        return;
                    }
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }

            if (!drainRunning || h264Socket == null || !h264Socket.isConnected()) {
                Log.w(TAG, "编码器输出循环：TCP连接未建立，退出");
                return;
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (drainRunning) {
                try {
                    int idx = videoEncoder.dequeueOutputBuffer(info, 10000);
                    if (idx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        continue;
                    } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        Log.i(TAG, "编码器输出格式变化: " + videoEncoder.getOutputFormat());
                    } else if (idx >= 0) {
                        if (info.size > 0 && h264Output != null) {
                            ByteBuffer buf = videoEncoder.getOutputBuffer(idx);
                            if (buf != null) {
                                buf.position(info.offset);
                                buf.limit(info.offset + info.size);
                                byte[] data = new byte[info.size];
                                buf.get(data);
                                try {
                                    h264Output.write(data);
                                    h264Output.flush();
                                } catch (IOException io) {
                                    Log.e(TAG, "写入H264失败:" + io.getMessage());
                                    closeSocketQuietly();
                                    break; // 连接断开，退出循环
                                }
                            }
                        }
                        videoEncoder.releaseOutputBuffer(idx, false);
                    }
                } catch (Exception e) {
                    if (drainRunning) {
                        Log.e(TAG, "编码输出循环异常", e);
                        break; // 异常时退出循环
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "编码器输出循环整体异常", e);
        } finally {
            closeSocketQuietly();
            Log.i(TAG, "编码写入线程结束");
        }
    }

    /**
     * 执行FFmpeg命令进行屏幕推流
     */
    private void executeFFmpegCommand() {
        try {
            // 构建FFmpeg命令
            String command = buildFFmpegCommand();
            if (command == null || command.isEmpty()) {
                Log.e(TAG, "executeFFmpegCommand：无法构建FFmpeg命令");
                releaseResources();
                return;
            }

            Log.i(TAG, "executeFFmpegCommand：执行FFmpeg命令：" + command);

            // 异步执行FFmpeg命令并接收日志/统计/完成回调
            ffmpegSession = FFmpegKit.executeAsync(
                    command,
                    session -> {
                        ReturnCode returnCode = session.getReturnCode();
            if (ReturnCode.isSuccess(returnCode)) {
                            Log.i(TAG, "FFmpeg 完成：推流正常结束");
            } else if (ReturnCode.isCancel(returnCode)) {
                            Log.i(TAG, "FFmpeg 完成：推流被取消");
            } else {
                            String fail = session.getFailStackTrace();
                            Log.e(TAG, "FFmpeg 失败：code=" + (returnCode != null ? returnCode.getValue() : -1)
                                    + ", msg=" + fail + ", output=" + session.getOutput());
                        }
                        releaseResources();
                    },
                    log -> {
                        // FFmpeg 详细日志
                        if (log != null && log.getMessage() != null) {
                            Log.d(TAG, "FFmpeg日志: " + log.getMessage());
                        }
                    },
                    statistics -> {
                        // 可按需上报统计信息，如速度/帧率/比特率等
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "executeFFmpegCommand：FFmpeg执行异常", e);
            releaseResources();
        }
    }

    /**
     * 构建FFmpeg命令字符串
     * 使用安卓设备屏幕作为输入源，编码后推流到RTMP服务器
     */
    private String buildFFmpegCommand() {
        try {
            // 仅用 FFmpegKit 命令行：用内置测试源验证 RTMP 通路
            // 说明：Android 端 FFmpeg 无法直接抓屏，若需屏幕推送需另行采集并喂入 FFmpeg 管道；
            // 这里先用 testsrc 验证 RTMP 服务与推流命令是否正常。
            StringBuilder commandBuilder = new StringBuilder();
            
            // 分支：如果是 file:// 则写入本地文件（便于在设备上快速验证编码链路）
            if (rtmpUrl != null && rtmpUrl.startsWith("file://")) {
                String outPath = rtmpUrl.substring("file://".length());
                commandBuilder
                        // 视频测试源
                        .append("-f lavfi ")
                        .append("-i testsrc=size=")
                        .append(width).append("x").append(height)
                        .append(":rate=").append(frameRate).append(" ")
                        // 静音音频源
                        .append("-f lavfi ")
                        .append("-i anullsrc=r=44100:cl=stereo ")
                        // 选择映射
                        .append("-map 0:v:0 -map 1:a:0 ")
                        // 编码参数（保持与 RTMP 一致，验证编码链路）
                        .append("-pix_fmt yuv420p ")
                        .append("-c:v h264_mediacodec ")
                        .append("-r ").append(frameRate).append(" ")
                        .append("-s ").append(width).append("x").append(height).append(" ")
                        .append("-b:v ").append(bitrate).append(" ")
                        .append("-g ").append(frameRate * 2).append(" ")
                        .append("-c:a aac -b:a 128k -ar 44100 -ac 2 ")
                        // 限制录制时长 10 秒并优化 MP4 头
                        .append("-t 10 -movflags +faststart ")
                        .append("-y ")
                        .append(outPath);

                return commandBuilder.toString();
            }

            // 推流前检测网络连通性，自动选择可达端口（原端口不可达时在 1935/80/443 间回退）
            String reachableRtmpUrl = getReachableRtmpUrl(rtmpUrl);
            commandBuilder
                    // 从本地 TCP 读取 H.264 屏幕编码数据
                    .append("-f h264 -r ").append(frameRate).append(" ")
                    .append("-i tcp://127.0.0.1:").append(localH264Port).append("?listen=1 ")
                    // 添加静音音频
                    .append("-f lavfi -i anullsrc=r=44100:cl=stereo ")
                    // 映射音视频流
                    .append("-map 0:v:0 -map 1:a:0 ")
                    // 视频直接复制（不重新编码）
                    .append("-c:v copy ")
                    // 音频编码：AAC
                    .append("-c:a aac -b:a 128k ")
                    // 输出到RTMP
                    .append("-f flv ")
                    .append("-rw_timeout 10000000 ")
                    .append("\"").append(reachableRtmpUrl).append("\"");

            return commandBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "buildFFmpegCommand：构建FFmpeg命令异常", e);
            return null;
        }
    }

    // 解析 RTMP URL 并测试端口连通性，优先使用原端口；不可达时回退到 1935/80/443
    private String getReachableRtmpUrl(String inputUrl) {
        if (inputUrl == null || !inputUrl.startsWith("rtmp://")) return inputUrl;
        try {
            URI uri = new URI(inputUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            if (host == null || host.isEmpty()) return inputUrl;

            if (port > 0 && isTcpReachable(host, port, 2000)) {
                Log.i(TAG, "RTMP 端口可达：" + host + ":" + port);
                return inputUrl;
            }

            int[] candidates = new int[]{1935, 80, 443};
            for (int cand : candidates) {
                if (cand == port) continue;
                if (isTcpReachable(host, cand, 2000)) {
                    Log.i(TAG, "选择可达 RTMP 端口：" + host + ":" + cand);
                    String base = "rtmp://" + host + ":" + cand + (path != null ? path : "");
                    if (query != null && !query.isEmpty()) base += "?" + query;
                    return base;
                }
            }

            Log.w(TAG, "未找到可达 RTMP 端口，使用原始地址");
            return inputUrl;
        } catch (URISyntaxException e) {
            Log.w(TAG, "RTMP URL 解析失败，直接使用原始地址：" + e.getMessage());
            return inputUrl;
        }
    }

    private boolean isTcpReachable(String host, int port, int timeoutMs) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (IOException ignore) {}
            }
        }
    }

    /**
     * 释放所有资源
     */
    private void releaseResources() {
        if (!isStreaming.get()) {
            return;
        }

        isStreaming.set(false);

        // 1. 释放屏幕采集资源
        releaseScreenCaptureResources();

        // 3. 停止FFmpeg会话
        if (ffmpegSession != null) {
            try {
                if (ffmpegSession.getState() == SessionState.RUNNING) {
                FFmpegKit.cancel(ffmpegSession.getSessionId());
                Log.i(TAG, "releaseResources：已取消FFmpeg会话");
                }
            } catch (Exception e) {
                Log.e(TAG, "releaseResources：取消FFmpeg会话异常", e);
            }
            ffmpegSession = null;
        }

        // 4. 释放MediaProjection（注意：由调用方决定是否释放）
        // 这里不释放MediaProjection，由调用方管理
        
        Log.i(TAG, "releaseResources：所有推流资源已释放");
    }

    /**
     * 关闭本地 TCP 连接
     */
    private void closeSocketQuietly() {
        if (h264Output != null) {
            try { h264Output.close(); } catch (Exception ignored) {}
            h264Output = null;
        }
        if (h264Socket != null) {
            try { h264Socket.close(); } catch (Exception ignored) {}
            h264Socket = null;
        }
    }

    /**
     * 获取推流状态
     * @return true：正在推流；false：未推流
     */
    public boolean isStreaming() {
        return isStreaming.get();
    }

    /**
     * 设置自定义的推流参数
     * 注意：此方法必须在startLive之前调用才能生效
     * @param width 视频宽度
     * @param height 视频高度
     * @param frameRate 帧率
     * @param bitrate 码率
     */
    public void setStreamParameters(int width, int height, int frameRate, int bitrate) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.bitrate = bitrate;
        Log.i(TAG, "setStreamParameters：已设置推流参数 - 分辨率：" + width + "x" + height + ", 帧率：" + frameRate + ", 码率：" + bitrate);
    }
}