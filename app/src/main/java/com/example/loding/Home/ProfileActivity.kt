package com.example.loding.Home

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.loding.Login.LoginActivity
import com.example.loding.R
import androidx.core.content.edit
import com.bumptech.glide.Glide

class ProfileActivity : AppCompatActivity() {
    private lateinit var ivAvatar: ImageView

    private lateinit var tvUsername: TextView
    private lateinit var tvGrade: TextView
    private lateinit var btnLogout: Button
    private lateinit var tv_item_profile: LinearLayout
    private lateinit var tv_item_settings: LinearLayout
    private lateinit var tv_item_about: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.iv_avatar) // 添加头像ImageView的引用
        tvUsername = findViewById(R.id.tv_username)
        tvGrade = findViewById(R.id.tv_email)
        btnLogout = findViewById(R.id.btn_logout)
        tv_item_profile=findViewById(R.id.item_profile)
        tv_item_settings=findViewById(R.id.item_settings)
        tv_item_about=findViewById(R.id.item_about)
        // 设置工具栏
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 添加返回按钮点击事件
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    //加载用户信息
    private fun loadUserData() {
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val username = preferences.getString("username", "用户") ?: "用户"
        val grade = preferences.getString("${username}_grade", "user@example.com") ?: "user@example.com"
        val uriString = preferences.getString("${username}_uri", null)
        val path = preferences.getString("${username}_path", null)

        tvUsername.text = username
        tvGrade.text = grade
        when {
            uriString != null -> {
                Glide.with(this)
                    .load(Uri.parse(uriString))
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivAvatar)
            }
            path != null -> {
                Glide.with(this)
                    .load(path)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(ivAvatar)
            }
            else -> {
                // 使用默认头像
                ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
        }
    }





    private fun setupListeners() {
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        tv_item_profile.setOnClickListener {
            Toast.makeText(this, "个人资料", Toast.LENGTH_SHORT).show()
        }

        tv_item_settings.setOnClickListener {
            Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show()
        }

        tv_item_about.setOnClickListener {
            Toast.makeText(this, "关于我们", Toast.LENGTH_SHORT).show()
        }
    }




    // 退出登录
    private fun showLogoutConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("确认退出")
        builder.setMessage("确定要退出登录吗？")
        builder.setPositiveButton("确定") { dialog, which ->
            // 清除登录状态
            val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            preferences.edit {
                // 只清除登录相关的状态，保留用户数据
                remove("is_logged_in")
                remove("username")
                remove("phone")
            }

            // 跳转到登录页面
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }


}