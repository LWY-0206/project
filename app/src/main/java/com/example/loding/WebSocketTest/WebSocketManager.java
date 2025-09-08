package com.example.loding.WebSocketTest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {
    private static volatile WebSocketManager instance;
    private OkHttpClient mClient;
    private WebSocket mWebSocket;
    private String mWsUrl;
    private WebSocketListener mListener;

    // 私有构造函数
    private WebSocketManager() {
        // 初始化 OkHttpClient
        mClient = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .connectTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    // 单例模式
    public static WebSocketManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketManager.class) {
                if (instance == null) {
                    instance = new WebSocketManager();
                }
            }
        }
        return instance;
    }

    // 设置回调监听
    public void setWebSocketListener(WebSocketListener listener) {
        this.mListener = listener;
    }

    // 连接 WebSocket
    public void connect(String url) {
        if (mWebSocket != null) {
            mWebSocket.cancel(); // 取消现有连接
        }
        this.mWsUrl = url;

        Request request = new Request.Builder()
                .url(url)
                .build();

        mWebSocket = mClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                if (mListener != null) {
                    mListener.onOpen(webSocket, response);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                if (mListener != null) {
                    mListener.onMessage(webSocket, text);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                if (mListener != null) {
                    mListener.onMessage(webSocket, bytes);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                if (mListener != null) {
                    mListener.onClosed(webSocket, code, reason);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                super.onFailure(webSocket, throwable, response);
                if (mListener != null) {
                    mListener.onFailure(webSocket, throwable, response);
                }
                // 可以在这里实现自动重连逻辑
                reconnect();
            }
        });
    }

    // 发送消息
    public boolean sendMessage(String msg) {
        if (mWebSocket != null) {
            return mWebSocket.send(msg);
        }
        return false;
    }

    // 关闭连接
    public void close(int code, String reason) {
        if (mWebSocket != null) {
            mWebSocket.close(code, reason);
            mWebSocket = null;
        }
    }

    // 重连机制
    private void reconnect() {
        // 避免频繁重连，添加延迟
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3秒后重连
                if (mWsUrl != null) {
                    connect(mWsUrl);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}