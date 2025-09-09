package com.example.loding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loding.Home.Home
import com.example.loding.Login.LoginActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查登录状态
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn: Boolean = preferences.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            // 已登录，跳转到主页
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        } else {
            // 未登录，跳转到登录页
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}