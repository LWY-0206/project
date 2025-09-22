package com.jxdx.login

//根据id获取用户信息
data class UserInfo(
    val userId: Int,             //用户ID
    val userName: String,           //用户名
    val avatarUrl: String? = null,  //头像
    val phone: String,              //手机号
    val profile: String,            //个人简介
    val status:String,              //状态：0禁用，1启用
    val identity: Int,              //身份：0-学生，1-老师
    val className: String,          //班级名称
)
//登录
data class LoginResponse(
    val userId: Int,
    val satoken: String
)