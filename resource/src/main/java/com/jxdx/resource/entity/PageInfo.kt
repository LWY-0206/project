
package com.jxdx.resource.entity

data class PageInfo(
    val current: Int, // 对应后端page.current（当前页码，如1）
    val pages: Int, // 对应后端page.pages（总页数，如167）
    val size: Int, // 对应后端page.size（每页条数，如5）
    val total: Int,
)