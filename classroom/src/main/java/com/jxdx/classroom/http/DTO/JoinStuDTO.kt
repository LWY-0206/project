package com.jxdx.classroom.http.DTO

import com.jxdx.classroom.group.Student

data class JoinStuDTO(
    val teamId: Int,
    val student: Student
)