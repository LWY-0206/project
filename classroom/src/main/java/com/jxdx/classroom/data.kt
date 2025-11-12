package com.jxdx.classroom

//课堂课程卡片
data class Subject(
    val name: String,
    val iconRes: Int
)
data class UserInfo (
    val id: Int,             //用户ID
    val userName: String,           //用户名
    val avatarUrl: String? = null,  //头像
    val phone: String,              //手机号
    val profile: String,            //个人简介
    val status:String,              //状态：0禁用，1启用
    val identity: Int,              //身份：0-学生，1-老师
    val className: String,          //班级名称
)

//查看所有课程
data class AllCourse(
    val subjectId: Int,
    val subjectName: String,          // 课程名
    val teacherName: String,   // 任课老师
    val avatarUrl: String,          //老师头像
    val status:Int,
)



//老师对应的学科
data class SubjectsVO(
    val subjectId:Int,
    val subjectName:String,
)