package Schedule.FirstPage

data class ScheduleItem(
    val week: String,           // 第几周
    val weekday: String,        // 星期几
    val courseName: String,     // 课程名称
    val courseTime: String,     // 上课时间
    val location: String,       // 上课地点
    val isCurrent: Int, // 是否是当前课程
    val courseId: Int,
    val teachName: String
)