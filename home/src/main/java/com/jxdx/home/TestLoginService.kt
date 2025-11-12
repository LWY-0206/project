package com.jxdx.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class TestLoginService : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HomeViewModel().checkLogin()
    }
}