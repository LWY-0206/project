package com.jxdx.mine.http.vo

import java.io.Serializable

data class CorrectedHomeworkDetailVO(
    val homeworkId: Int? = null,
    val studentId: Int? = null,
    val studentName: String? = null,
    val teacherComment: String? = null,
    val score: Double? = null
) : Serializable
