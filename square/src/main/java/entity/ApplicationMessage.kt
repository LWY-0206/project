package entity

import com.example.corekit.recyclerview.MultipleType

data class ApplicationMessage(
    // 用户头像URL
    val applicantAvatar: String,
    // 用户名
    var applicantName: String,
    // 时间
    var createTime: String,
    // 说明
    var remark: Int,
) : MultipleType {
    override fun viewType(): Int = 1
}
