package com.example.loding.Famous

data class FamousDetailResponse(
    val code: Int,
    val message: String,
    val data: FamousDetail
)

data class FamousDetail(
    val celebrityId: Int,
    val celebrityName: String,
    val profession: String,
    val era: String,
    val description: String,
    val avatarUrl: String
)