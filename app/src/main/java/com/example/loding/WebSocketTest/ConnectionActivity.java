package com.example.loding.WebSocketTest;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loding.R;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket连接（整合版）
 * 支持通过请求头携带Sa-Token，与指定用户聊天
 *
 * @author YourName
 * @since 2024-01-01
 */
public class ConnectionActivity extends AppCompatActivity {
    // 配置参数
    private String saToken = "65979652-9aed-4219-b856-c970c9bcfc61"; // 你的Sa-Token
    private int targetUserId = 1; // 聊天对象的用户ID
    private String wsBaseUrl = "ws://121.41.176.238:8080/single/chat/"; // 基础WebSocket地址

    // 控件
    private TextView tvStatus;
    private TextView tvMessage;
    private EditText etInput;
    private Button btnSend;
    private Button btnConnect;

    // WebSocket相关
    private OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private boolean isConnected = false;
    private Handler mainHandler; // 主线程更新UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // 初始化主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());

        // 初始化控件
        tvStatus = findViewById(R.id.tv_status);
        tvMessage = findViewById(R.id.tv_message);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnConnect = findViewById(R.id.btn_connect);

        // 初始化OkHttpClient（带Sa-Token拦截器）
        initOkHttpClient();

        // 初始禁用发送按钮
        btnSend.setEnabled(false);

        // 连接按钮点击事件
        btnConnect.setOnClickListener(v -> {
            if (isConnected) {
                // 断开连接
                disconnect();
            } else {
                // 建立连接
                connect();
            }
        });

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> {
            String msg = etInput.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(msg);
        });
    }

    // 初始化OkHttpClient，添加Sa-Token请求头拦截器
    private void initOkHttpClient() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws java.io.IOException {
                        // 给所有请求添加Sa-Token头
                        Request newRequest = chain.request()
                                .newBuilder()
                                .addHeader("satoken", saToken) // 关键：通过请求头携带Token
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
    }

    // 建立WebSocket连接
    private void connect() {
        // 检查Token是否有效
        if (TextUtils.isEmpty(saToken)) {
            Toast.makeText(this, "Sa-Token为空，请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新UI状态
        tvStatus.setText("连接状态：正在连接...");
        tvStatus.setTextColor(Color.BLUE);
        btnSend.setEnabled(false);

        // 构建完整连接地址（基础地址 + 目标用户ID）
        String fullWsUrl = wsBaseUrl + targetUserId;

        // 建立连接
        Request request = new Request.Builder()
                .url(fullWsUrl)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                isConnected = true;
                mainHandler.post(() -> {
                    tvStatus.setText("连接状态：已连接（与用户 " + targetUserId + " 聊天中）");
                    tvStatus.setTextColor(Color.GREEN);
                    Toast.makeText(ConnectionActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                    btnConnect.setText("断开连接");
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                mainHandler.post(() -> {
                    // 显示收到的消息（对方发送的消息）
                    String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                    tvMessage.append("[对方 " + time + "]：" + text + "\n");
                    scrollToBottom();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                // 处理二进制消息（一般聊天场景用不到）
                mainHandler.post(() -> {
                    tvMessage.append("[系统] 收到二进制消息\n");
                    scrollToBottom();
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                super.onFailure(webSocket, throwable, response);
                isConnected = false;
                mainHandler.post(() -> {
                    String errorMsg = "连接失败：";
                    if (throwable.getMessage() != null) {
                        errorMsg += throwable.getMessage();
                    } else {
                        errorMsg += "未知错误";
                    }
                    if (response != null) {
                        errorMsg += "，响应码：" + response.code();
                        // 401通常表示Token无效或未登录
                        if (response.code() == 401) {
                            errorMsg += "（Token无效或已过期）";
                        }
                    }
                    tvStatus.setText(errorMsg);
                    tvStatus.setTextColor(Color.RED);
                    btnSend.setEnabled(false);
                    btnConnect.setText("连接");
                });
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                isConnected = false;
                mainHandler.post(() -> {
                    tvStatus.setText("连接状态：已关闭（原因：" + reason + "）");
                    tvStatus.setTextColor(Color.GRAY);
                    btnSend.setEnabled(false);
                    btnConnect.setText("连接");
                });
            }
        });
    }

    // 断开WebSocket连接
    private void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "用户主动断开");
            webSocket = null;
        }
        isConnected = false;
        tvStatus.setText("连接状态：已断开");
        tvStatus.setTextColor(Color.GRAY);
        btnSend.setEnabled(false);
        btnConnect.setText("连接");
    }

    // 发送消息
    private void sendMessage(String msg) {
        if (!isConnected || webSocket == null) {
            Toast.makeText(this, "未连接，请先建立连接", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isSuccess = webSocket.send(msg);
        if (isSuccess) {
            etInput.setText("");
            // 显示自己发送的消息
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            tvMessage.append("[我 " + time + "]：" + msg + "\n");
            scrollToBottom();
        } else {
            Toast.makeText(this, "发送失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    // 滚动到最新消息
    private void scrollToBottom() {
        ((ScrollView) tvMessage.getParent()).fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect(); // 页面销毁时断开连接
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
