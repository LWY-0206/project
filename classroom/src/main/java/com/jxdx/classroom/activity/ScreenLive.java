package com.jxdx.classroom.activity;

import android.media.projection.MediaProjection;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class ScreenLive extends Thread {
   private String url;
   private MediaProjection mediaProjection;
   private final LinkedBlockingQueue<RTMPPackage> queue =
           new LinkedBlockingQueue<>();//堵塞队列
   private boolean isLiving;

   static {
       System.loadLibrary("native-lib");
   }

   public void startLive(String url, MediaProjection mediaProjection) {
       this.url = url;
       this.mediaProjection = mediaProjection;
       start();
   }

   @Override
   public void run() {
       if (!connect(url)) {
           Log.i("liuyi", "run: ----------->推送失败");
           return;
       }

       Log.i("liuyi", "run: ----------->连接接成功");
       VideoCodec videoCodec = new VideoCodec(this);
       videoCodec.startLive(mediaProjection);

       isLiving = true;
       while (isLiving) {
           RTMPPackage rtmpPackage = null;
           try {
               rtmpPackage = queue.take();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           if (rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0) {
               sendData(rtmpPackage.getBuffer(), rtmpPackage.getBuffer() .length , rtmpPackage.getTms());
           }
       }
   }

   public void addPackage(RTMPPackage rtmpPackage) {
       if (!isLiving) {
           return;
       }
       queue.add(rtmpPackage);
   }

   private native boolean connect(String url);

   private native boolean sendData(byte[] data, int len, long tms);

   private native void close();
}
