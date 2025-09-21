package com.jxdx.classroom.activity;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 将采集到的视频流编码为H264,并加入rtmp队列
 */
public class VideoCodec extends  Thread {
   private MediaProjection mediaProjection;
   private VirtualDisplay virtualDisplay;
   private MediaCodec mediaCodec;
   private ScreenLive screenLive;
   private volatile boolean isLiving;
   private long timeStamp;//时间戳
   private long startTime;//开始时间

   public VideoCodec(ScreenLive screenLive) {
       this.screenLive = screenLive;
   }

   public void startLive(MediaProjection mediaProjection) {
       this.mediaProjection = mediaProjection;
       MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
       format.setInteger(MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
       format.setInteger(MediaFormat.KEY_BIT_RATE, 400_000);//码率
       format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);//帧率
       format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);//关键帧间隔时间 单位为秒
       try {
           mediaCodec =
                   MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);//编码类型为H264
           mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
           Surface surface = mediaCodec.createInputSurface();
           virtualDisplay = mediaProjection.createVirtualDisplay(
                   "screen-codec",
                   720, 1280, 1,
                   DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                   surface, null, null);
       } catch (IOException e) {
           e.printStackTrace();
       }
       start();
   }

   @Override
   public void run() {
       isLiving = true;
       mediaCodec.start();
       MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
       while (isLiving) {

           //每隔两秒手动触发一次关键帧
           if (System.currentTimeMillis() - timeStamp >= 2000) {
               Bundle params = new Bundle();
               params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
               mediaCodec.setParameters(params);
               timeStamp = System.currentTimeMillis();
           }

           int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);

           if (index >= 0) {
               if (startTime == 0) {
                   startTime = bufferInfo.presentationTimeUs / 1000;
               }
               ByteBuffer buffer = mediaCodec.getOutputBuffer(index);

               byte[] outData = new byte[bufferInfo.size];
               buffer.get(outData);
               RTMPPackage rtmpPackage = new RTMPPackage(outData, (bufferInfo.presentationTimeUs / 1000) - startTime);
               screenLive.addPackage(rtmpPackage);//加入RTMP发送队列
               mediaCodec.releaseOutputBuffer(index, false);
           }
       }

       isLiving = false;
       mediaCodec.stop();
       mediaCodec.release();
       mediaCodec = null;
       virtualDisplay.release();
       virtualDisplay = null;
       mediaProjection.stop();
       mediaProjection = null;
       startTime = 0;
   }

   public void close(){
       isLiving = false;
   }


}