package com.jxdx.resource.Famous

data class FamousDetailResponse(
    val code: Int,
    val message: String,
    val data: FamousDetail
)

data class FamousDetail(
    val celebrityId: Int,
    var celebrityName: String,
    val profession: String,
    val era: String,
    val description: String,
    val avatarUrl: String
)