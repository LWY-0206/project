package com.example.loding.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.home.Home
import com.jxdx.login.Login.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查登录状态
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn: Boolean = preferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            // 已登录，获取用户信息并跳转到主页
//            Toast.makeText(this, "id:"+preferences.getInt("user_id",1), Toast.LENGTH_SHORT).show()
            //根据id获取用户信息
//                ....
            //保存用户信息
//            preferences.edit {
//                putBoolean("is_logged_in", true)
//                putInt("user_id", it.data?.userId?:1)
//                putString("user_name", it.data?.userName)
//                putString("avatar_url", it.data?.avatarUrl ?: "")
//                putString("user_phone", it.data?.phone)
//                putString("user_identity", it.data?.identity.toString())
//                putString("user_className", it.data?.className)
//                putString("user_bio", it.data?.bio)
//            }

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