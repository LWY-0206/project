package com.example.loding.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loding.Home.Home;
import com.example.loding.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private RadioGroup rgIdentity;
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private boolean isteacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        rgIdentity=findViewById(R.id.rg_identity);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
    }

    //立即注册监听事件
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                performLogin();
            }
        });

        tvRegister.setOnClickListener(v -> {
            // 跳转到注册页面
            Intent  intent=new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();

        });
    }

    //
    private boolean validateInput() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;

        if (username.isEmpty()) {
            tilUsername.setError("用户名不能为空");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("密码不能为空");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("密码长度不能少于6位");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    //登录逻辑
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 模拟网络请求
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();

            // 模拟登录成功
            if (isValidUser(username, password)) {
                // 保存登录状态
                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("username", username);
                editor.putString("email", preferences.getString(username + "_email", ""));
                editor.apply();

                // 跳转到主页
                if(preferences.getString(username+"_identity","").equals("教师")){
                    //如果识别用户名身份为教师，跳转到教师端
                    Intent intent = new Intent(LoginActivity.this, Home.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this,"已登录教师端",Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    //学生端
                    Intent intent = new Intent(LoginActivity.this, Home.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this,"已登录学生端",Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private boolean isValidUser(String username, String password) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // 检查用户是否存在
        String savedPassword = preferences.getString(username + "_password", null);
        if (savedPassword == null) {
            return false; // 用户不存在
        }

        // 检查密码是否正确
        return savedPassword.equals(password);
    }
}
