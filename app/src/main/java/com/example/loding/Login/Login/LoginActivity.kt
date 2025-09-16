package com.example.loding.Login

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.edit
import com.example.loding.Home.Home
import com.example.loding.R

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        tilUsername = findViewById(R.id.til_username)
        tilPassword = findViewById(R.id.til_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
    }

    // 立即注册监听事件
    private fun setupListeners() {
        btnLogin.setOnClickListener {
            if (validateInput()) {
                performLogin()
            }
        }

        tvRegister.setOnClickListener {
            // 跳转到注册页面
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(): Boolean {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true

        if (username.isEmpty()) {
            tilUsername.error = "用户名不能为空"
            isValid = false
        } else {
            tilUsername.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "密码不能为空"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "密码长度不能少于6位"
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    // 登录逻辑
    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // 显示加载对话框
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("登录中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // 模拟网络请求
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()

            // 模拟登录成功
            if (isValidUser(username, password)) {
                // 保存登录状态
                val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                preferences.edit {
                    putBoolean("is_logged_in", true)
                    putString("username", username)//确定用户的username
                }

                // 跳转到主页
                if (preferences.getString("${username}_identity", "") == "教师") {
                    // 如果识别用户名身份为教师，跳转到教师端
                    val intent = Intent(this@LoginActivity, Home::class.java)
                    startActivity(intent)
                    Toast.makeText(this@LoginActivity, "已登录教师端", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // 学生端
                    val intent = Intent(this@LoginActivity, Home::class.java)
                    startActivity(intent)
                    Toast.makeText(this@LoginActivity, "已登录学生端", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT).show()
            }
        }, 1500)
    }

    private fun isValidUser(username: String, password: String): Boolean {
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // 检查用户是否存在
        val savedPassword = preferences.getString("${username}_password", null)
        if (savedPassword == null) {
            return false // 用户不存在
        }

        // 检查密码是否正确
        return savedPassword == password
    }
}