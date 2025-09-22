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
data class Member(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val role: String? = null,
    val itemType: Int     //0是身份 区分，1老师2学生
)
data class ApiMember(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val role: String?,
    val type: Int // 1=老师, 2=学生
)
//课程
data class Course(
    val id: String,
    val name: String,          // 课程名
    val teacherName: String,   // 任课老师
    val progress: String       // 课程进度描述，如"8/12"
)

// 历史课件数据
data class Courseware(
    val id: String,
    val courseId: String,
    val title: String,         // 课件名称
    val uploadTime: String,    // 上传时间
    val fileUrl: String        // 文件下载或预览地址
)

// 学习报告
data class StudyReport(
    val totalStudyTime: String,  // 总学习时长
    val completedHomework: Int,  // 已完成作业数
    val totalHomework: Int,      // 作业总数
    val averageScore: Int        // 平均得分
)