package com.jxdx.classroom.entity

/**
 * 课堂设置实体类
 */
data class ClassroomSetting(
    val subjectId: Int,
    val teacherId: Int,
    val allowStudentChat: Boolean = true,
    val allowStudentRaiseHand: Boolean = true,
    val allowStudentDraw: Boolean = false,
    val autoMuteStudents: Boolean = true,
    val enableRecording: Boolean = true,
    val enableScreenShare: Boolean = true,
    val maxStudents: Int = 30,
    val classDuration: Int = 45, // 分钟
    val breakTime: Int = 10 // 分钟
) {
    companion object {
        fun getDefaultSettings(subjectId: Int, teacherId: Int): ClassroomSetting {
            return ClassroomSetting(
                subjectId = subjectId,
                teacherId = teacherId,
                allowStudentChat = true,
                allowStudentRaiseHand = true,
                allowStudentDraw = false,
                autoMuteStudents = true,
                enableRecording = true,
                enableScreenShare = true,
                maxStudents = 30,
                classDuration = 45,
                breakTime = 10
            )
        }
    }
}
