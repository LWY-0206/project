package com.jxdx.resource.entity

import com.example.corekit.recyclerview.MultipleType

// 学校列表项
data class SchoolItem(
    val schoolId: Int,
    val schoolName: String,
    val emblemUrl: String
) : MultipleType {
    override fun viewType(): Int {
        return ViewType.SCHOOL_ITEM
    }
}

// 加载更多项
data class LoadingItem(
    val message: String = "加载中..."
) : MultipleType {
    override fun viewType(): Int {
        return ViewType.LOADING_ITEM
    }
}

// 错误项
data class ErrorItem(
    val errorMsg: String,
    val retry: (() -> Unit)? = null
) : MultipleType {
    override fun viewType(): Int {
        return ViewType.ERROR_ITEM
    }
}

// 空状态项
data class EmptyItem(
    val message: String = "暂无数据"
) : MultipleType {
    override fun viewType(): Int {
        return ViewType.EMPTY_ITEM
    }
}

object ViewType {
    const val SCHOOL_ITEM = 1
    const val LOADING_ITEM = 2
    const val ERROR_ITEM = 3
    const val EMPTY_ITEM = 4
}