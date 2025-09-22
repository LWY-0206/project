package com.jxdx.classroom.http

data class UserInfo (
    val userId: Int,             //用户ID
    val userName: String,           //用户名
    val avatarUrl: String? = null,  //头像
    val phone: String,              //手机号
    val profile: String,            //个人简介
    val status:String,              //状态：0禁用，1启用
    val identity: Int,              //身份：0-学生，1-老师
    val className: String,          //班级名称
)
