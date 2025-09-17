package com.jxdx.resource.Schools

import com.example.corekit.recyclerview.MultipleType
import com.jxdx.resource.entity.PageInfo

data class SchoolsResponse(
    val code: Int,
    val message: String,
    val data: SchoolData,
)

data class SchoolData(
    val records: List<SchoolRecord>,
    val total: Int,
    val size: Int,
    val current: Int,
    val page: PageInfo
)

data class SchoolRecord(
    val schoolId: Int,
    val schoolName: String,
    val emblemUrl: String
) : MultipleType {
    override fun viewType(): Int {
        return ViewType.SCHOOL_ITEM
    }
}

// 加载项
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
    val message: String = "暂无学校数据"
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