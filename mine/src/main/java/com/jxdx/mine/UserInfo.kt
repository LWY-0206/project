package com.jxdx.mine

data class UserInfo(
    val userId: String,             //用户ID
    val userName: String,           //用户名
    val avatarUrl: String,          //头像
    val phone: String,              //手机号
    val profile: String,            //个人简介
    val status: Int,                 //	状态：0禁用，1启用
    val identity: Int,              //身份：0-学生，1-老师
    val className: String,          //班级名称
    val password: String            //密码
)