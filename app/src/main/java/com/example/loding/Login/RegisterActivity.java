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

//注册页
public class RegisterActivity extends AppCompatActivity {

    private RadioGroup rgIdentity;
    private TextInputEditText etUsername, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputLayout tilUsername, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private boolean isTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        rgIdentity=findViewById(R.id.rg_identity);

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                performRegistration();
            }
        });

        tvLogin.setOnClickListener(v -> {
            // 跳转到登录页面
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput() {
//        获取选中的身份
        int selectedId = rgIdentity.getCheckedRadioButtonId();
        isTeacher = (selectedId == R.id.rb_teacher);

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        // 验证用户名
        if (username.isEmpty()) {
            tilUsername.setError("用户名不能为空");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("用户名长度不能少于3位");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // 验证邮箱
        if (email.isEmpty()) {
            tilEmail.setError("邮箱不能为空");
            isValid = false;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("请输入有效的邮箱地址");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // 验证手机号
        if (phone.isEmpty()) {
            tilPhone.setError("手机号不能为空");
            isValid = false;
        } else if (!isValidPhone(phone)) {
            tilPhone.setError("请输入有效的手机号码");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // 验证密码
        if (password.isEmpty()) {
            tilPassword.setError("密码不能为空");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("密码长度不能少于6位");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("请确认密码");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("两次输入的密码不一致");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isValidPhone(String phone) {
        return phone.length() == 11;
    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("注册中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 模拟网络请求
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();

            // 模拟注册成功
            // 在实际应用中，这里应该发送注册请求到服务器
            // 并处理服务器的响应

            // 保存用户信息（模拟注册成功）
            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE); //MODE_PRIVATE私有模式
            //写入数据
            SharedPreferences.Editor editor = preferences.edit();

            // 使用用户名作为key的前缀来存储用户信息
            if(isTeacher)
                editor.putString(username+"_identity","教师");
            else
                editor.putString(username+"_identity","学生");
            editor.putString(username + "_password", password);
            editor.putString(username + "_email", email);
            editor.putString(username + "_phone", phone);


            editor.putBoolean("is_logged_in", true);
            //登录状态个人主页可能会需要的个人信息
            editor.putString("username", username);
            editor.putString("email", email);
            editor.putString("phone", phone);
            // 提交更改apply(): 异步写入磁盘，不会阻塞UI线程，没有返回值。
            //commit(): 同步写入磁盘，会阻塞UI线程直到写入完成，并返回一个 boolean 值表示成功与否。
            editor.apply();

            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

            //后续应该是直接跳转到进入应用的首页
            // 这里跳转到主页
            if(isTeacher){
                //跳转到教师端
                Intent intent = new Intent(RegisterActivity.this, Home.class);
                startActivity(intent);
                Toast.makeText(RegisterActivity.this,"已登录教师端",Toast.LENGTH_SHORT).show();
                finish();
            }else{
                //跳转到学生端
                Intent intent = new Intent(RegisterActivity.this, Home.class);
                startActivity(intent);
                Toast.makeText(RegisterActivity.this,"已登录学生端",Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 1500);
    }
}