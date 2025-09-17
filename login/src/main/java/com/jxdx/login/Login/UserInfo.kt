package com.jxdx.login.Login

data class UserInfo(
    val userId: Int,             //用户ID
    val userName: String,           //用户名
    val avatarUrl: String? = null,  //头像
    val phone: String,              //手机号
    val password: String,           //密码
    val identity: Int,              //身份：0-学生，1-老师
    val className: String,          //班级名称
    val bio: String                 //个人简介
)