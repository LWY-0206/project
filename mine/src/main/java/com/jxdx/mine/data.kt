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
    val homeworkId: String,             // 作业ID
    val subject: String,                // 所属科目
    val completeAndCorrect:Int,         // 当前状态0未提交/1已提交未批改/3已完成
    val homeworkName: String,           // 作业标题
    val deadTime: String,               // 截止日期
    val sendTime: String = ""
)

// 学生作业详情VO
data class StuHomeWorkDetailVO(
    val homeworkId: Long,               // 作业ID
    val subject: String,                // 所属科目
    val homeworkName: String,           // 作业名称
    val homeworkContent: String,        // 作业内容
    val deadTime: String,               // 截止时间
    val completeAndCorrect: Int,        // 1-未完成，2-已提交未批改，3-已批改
    val imageUrls: List<String>? = null, // 图片URL列表
    val studentContent: String? = null, // 提交内容（仅 status >= 2 时有）
    val submitTime: String? = null,     // 提交时间（仅 status >= 2 时有）
    val score: Double? = null,          // 分数（仅 status == 3 时有）
    val teacherComment: String? = null, // 教师评语（仅 status == 3 时有）
    val correctTime: String? = null     // 批改时间（仅 status == 3 时有）
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

//查看所有课程
data class Course(
    val subjectId: Int,
    val subjectName: String,          // 课程名
    val teacherName: String,   // 任课老师
    val avatarUrl: String,          //老师头像
    val status:Int,
)

//获取课程详情
data class CourseDetail(
    val subjectName: String,        //课程名称
    val subjectId: Int?,             //课程id
    val teacherName: String,        //老师名字
    val teacherId: Int,             //老师id
    val uploadTime: String,         // 上传时间
    val file: Map<String, String>?,  // 课件名称:文件地址
)
//将CourseDetail中的file：Map<String, String>转换为List<Courseware>
data class Courseware(
    val CoursewareName: String, // 文件描述
    val url: String,         // 文件下载地址
)


// 学习报告
data class StudyReport(
    val totalStudyTime: String,  // 总学习时长
    val completedHomework: Int,  // 已完成作业数
    val totalHomework: Int,      // 作业总数
    val averageScore: Int        // 平均得分
)


// 作业模型
data class HomeworkDetail(
    val id: String,
    var title: String,
    var description: String,
    var dueDate: String,
    val submissions: MutableList<StudentSubmission>
)
// 学生提交模型
data class StudentSubmission(
    val studentId: String,
    val studentName: String,
    val content: String, // 作业内容（文字/图片URL）
    var score: Int?,     // 老师评分
    var comment: String?,// 老师评语
    var isReviewed: Boolean // 是否批改
)

data class SubjectsVO(
    val subjectId: Int,
    val subjectName: String,
)