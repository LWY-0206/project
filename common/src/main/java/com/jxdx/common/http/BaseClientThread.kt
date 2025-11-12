package org.jxxy.debug.common.http

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.corekit.gson.GsonManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

open class BaseClientThread(private val handler: Handler,val ip:String="47.115.202.222",val port:Int = 39000,):Runnable {
    private val TAG = "BaseClientThread"
    private var mSocket: Socket? = null
    private var mBufferedReader: BufferedReader? = null
    private lateinit var mOutputStream: OutputStream
    lateinit var revHandler: Handler
    private var thread:Thread?=null
    private var isReading = true
    var fristMessage :String?=null
    override fun run() {
        try {
            mSocket = Socket(ip, port)
            mBufferedReader = BufferedReader(InputStreamReader(mSocket!!.getInputStream()))
            mOutputStream = mSocket!!.getOutputStream()
            fristMessage?.let {
                GlobalScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "run: 第一次发送了${it}")
                    mOutputStream.write((it + "\n").toByteArray(Charsets.UTF_8))
                }
            }
            // 读取后转发到主线程
            thread= Thread {
                try {
                    var content: String = ""
                    while (isReading && mBufferedReader!!.readLine().also { content = it } != null) {
                        val msg = Message()
                        msg.what = 0
                        msg.obj = content
                        Log.d(TAG, "handleMessage:发给主线程 ${msg.obj}")
                        handler.sendMessage(msg)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread!!.start()
            Looper.prepare()
            // 接收到主线程发过来的消息转成json后用socket发到后端
            revHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    if (msg.what == 0) {
                        try {
                            val gson = Gson()
                            val json = gson.toJson(msg.obj).replace("\n", "").replace("\r", "")
                            Log.d(TAG, "handleMessage:Socket线程接收到主线程发来的 ${json}")
                            val bytes = (json + "\n").toByteArray(Charsets.UTF_8)
                            GlobalScope.launch(Dispatchers.IO) {
                                mOutputStream.write(bytes)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            Looper.loop()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "")
        }
    }
    fun stopThread(last:Any ?= null) {
        try {
            isReading = false
            GlobalScope.launch {
                async(Dispatchers.IO){
                    val json = GsonManager.instance.gson.toJson(last).replace("\n", "").replace("\r", "")
                    Log.d(TAG, "stopThread:Socket线程接收到主线程最后发来的 ${json}")
                    val bytes = (json + "\n").toByteArray(Charsets.UTF_8)
                     mOutputStream.write(bytes)
                }.await()
                Log.d(TAG, "stopThread: a")
                thread?.join()
                Log.d(TAG, "stopThread: b")
                mBufferedReader?.close()
                mSocket?.close()
                revHandler.removeCallbacksAndMessages(null)
                handler.removeCallbacksAndMessages(null)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}