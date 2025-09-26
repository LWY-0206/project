package com.jxdx.classroom.com.jxdx.classroom.entity

import com.example.corekit.recyclerview.MultipleType

data class ClassLive (
    val liveId: Int,
    val roomName: String,
    val className: String,
    val teacherName: String,
    val subjectName: String,
    val startTime: String?,
    val status: Int
): MultipleType{
    override fun viewType():Int=1
}