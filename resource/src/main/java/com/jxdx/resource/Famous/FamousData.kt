package com.jxdx.resource.Famous

data class FamousR (
    val code:Int,
    val message:String,
    val data: FamousData
)
data class FamousData (
    val records: List<FamousRecord>,
    val total: Int,
    val size: Int,
    val current: Int,
    val orders: List<String>,
    val optimizeCountSql: Boolean,
    val searchCount: Boolean,
    val maxLimit: Int?,
    val countId: Int?,
    val page: Int
)
data class FamousRecord (
    val celebrityId: Int,
    val celebrityName: String,
    val profession: String,
    val era: String,
    val avatarUrl: String,
)