package com.jxdx.classroom.group


data class Student(
    val id: Int,
    val name: String,
    val avatarUrl: String?,      //头像
    var isLeader: Boolean = false,      //是否为组长
    val memberIndex:Int,
)

data class Group(
    val id: Int,
    val name: String,
    val capacity:Int,
    val currentCount: Int,
    var students: MutableList<Student?>
)


//小组讨论专用消息类
data class Message(
    val id: String,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val isFromTeacher: Boolean = false,
    val senderAvatar: String? = null
)

data class DiscussionState(
    val groupId: String,
    val onlineStudents: List<String>,
    val isTeacherPresent: Boolean = false,
    val lastActivityTime: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT, SYSTEM
}
