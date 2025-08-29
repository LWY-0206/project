package com.example.loding.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.loding.Login.LoginActivity;
import com.example.loding.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnLogout = findViewById(R.id.btn_logout);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //在应用栏的左侧显示一个返回按钮（Up Button），通常是一个向左的箭头（←）
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadUserData() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = preferences.getString("username", "用户");
        String email = preferences.getString("email", "user@example.com");

        tvUsername.setText(username);
        tvEmail.setText(email);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });

        findViewById(R.id.item_profile).setOnClickListener(v -> {
            Toast.makeText(this, "个人资料", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.item_settings).setOnClickListener(v -> {
            Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.item_about).setOnClickListener(v -> {
            Toast.makeText(this, "关于我们", Toast.LENGTH_SHORT).show();
        });
    }

    //退出登录
    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认退出");
        builder.setMessage("确定要退出登录吗？");
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 清除登录状态
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            // 只清除登录相关的状态，保留用户数据
            editor.remove("is_logged_in");
            editor.remove("username");
            editor.remove("email");
            editor.remove("phone");
            editor.apply();

            // 跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}