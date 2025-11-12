package com.jxdx.login.Login

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.edit
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.common.http.service.HomeService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.login.LoginResponse
import com.jxdx.login.R
import com.jxdx.login.UserInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // 立即登录注册监听事件
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
        val phone = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("登录中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val loginRequest = LoginRequest(phone, password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<BaseResp<LoginResponse>> {
            override fun onResponse(call: Call<BaseResp<LoginResponse>>, response: Response<BaseResp<LoginResponse>>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 0) {

                            // 保存 Token
                            it.data?.satoken?.let {
                                //持久化在本地 MMKV:保存 Token 到内存 + 本地存储，并更新请求头
                                TokenManager.login(it)
                            }

                            //保存登录状态
                            val preferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            preferences.edit {
                                putBoolean("is_logged_in", true)
                            }
                            // 跳转到主页
                            ServiceRegistry.get(HomeService::class.java)?.navigateToHome(this@LoginActivity)
                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "登录失败，错误代码: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BaseResp<LoginResponse>>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
            }
        })
    }
}