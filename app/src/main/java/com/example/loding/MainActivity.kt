package com.example.loding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loding.Home

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 跳转
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }
}