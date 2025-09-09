package com.example.loding.adapter

data class DynamicBody(
    val title: String,
    val content: String,
    val contentImageUrls: List<String>? = null,
)
