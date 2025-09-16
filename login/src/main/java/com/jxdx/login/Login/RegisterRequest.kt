package com.jxdx.login.Login

data class RegisterRequest(
    val userName: String,
    val avatarUrl: String? = null,
    val phone: String,
    val identity: Int,
    val password: String,
    val className: String,
)