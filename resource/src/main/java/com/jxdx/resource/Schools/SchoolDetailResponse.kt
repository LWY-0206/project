package com.jxdx.resource.Schools

data class SchoolDetailResponse(
    val code: Int,
    val message: String,
    val data: SchoolDetail
)

data class SchoolDetail(
    val schoolId: Int,
    val schoolName: String,
    val simpleAddress: String,
    val detailedAddress: String,
    val emblemUrl: String,
    val schoolProfile: String,
    val is985: Boolean,
    val is211: Boolean,
    val schoolScoreThisYear: Int,
    val schoolScoreLastYear: Int,
    val schoolScoreLastLastYear: Int
)