package com.jxdx.mine

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
// 单个作业
data class Homework(
    val id: String,          // 作业ID
    val title: String,       // 作业标题
    val deadline: String,    // 截止日期
    val subject: String,     // 所属科目
    var status: Int,         // 当前状态0未提交/1已提交未批改/3已完成
    val sendTime: String = ""
)

// 科目分组
class SubjectGroup(
    val subjectName: String,             // 科目名，如 数学、语文
    var isExpanded: Boolean = false,     // 是否展开
    val homeworkList: MutableList<Homework> // 科目下的作业列表
){
    // 添加一个唯一标识符用于比较
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SubjectGroup
        return subjectName == other.subjectName
    }

    override fun hashCode(): Int {
        return subjectName.hashCode()
    }
}
data class PageData<T>(
    val records: List<T>,
    val total: Int,
    val size: Int,
    val current: Int,
    val pages: Int
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