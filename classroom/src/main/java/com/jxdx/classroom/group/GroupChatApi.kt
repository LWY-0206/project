package com.jxdx.classroom.group

/**
 * 小组聊天API接口定义
 */
object GroupChatApi {
    /**
     * 获取小组历史消息接口
     * 接口路径: /groupChat/getHistory
     */
    object GetHistory {
        const val PATH = "/groupChat/getHistory"
        
        /**
         * 请求参数
         */
        data class RequestParams(
            val teamId: Int, // 必需，团队ID
            val pageNum: Int, // 必需，页码，示例值: 1
            val pageSize: Int // 必需，每页数量，示例值: 20
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int,
            val message: String,
            val data: Data
        ) {
            data class Data(
                val messages: List<Message>,
                val pageNum: Int,
                val pageSize: Int,
                val total: Int,
                val totalPages: Int,
                val hasMore: Boolean
            ) {
                data class Message(
                    val id: Int,
                    val fromUserId: Int,
                    val content: String,
                    val messageType: Int,
                    val sendTime: String
                )
            }
        }
    }
    
    /**
     * 保存小组聊天消息接口
     * 接口路径: /groupChat/saveMessage
     */
    object SaveMessage {
        const val PATH = "/groupChat/saveMessage"
        
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val teamId: Long? = null, // 可选，团队ID
            val fromUserId: Long? = null, // 可选，发送用户ID
            val content: String? = null, // 可选，消息内容
            val messageType: Int? = null // 可选，消息类型
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int,
            val message: String,
            val data: Int // 消息ID
        )
    }
    
    /**\n     * 发送消息接口
     * 接口路径: /groupChat/sendMessage
     */
    object SendMessage {
        const val PATH = "/groupChat/sendMessage"
        
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val teamId: Long? = null, // 可选，团队ID
            val fromUserId: Long? = null, // 可选，发送用户ID
            val content: String? = null, // 可选，消息内容
            val messageType: Int? = null // 可选，消息类型
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int? = null,
            val message: String? = null,
            val data: Long? = null // 消息ID
        )
    }
    
    /**
     * 老师统一广播给每个小组接口
     * 接口路径: /groupChat/broadcast
     */
    object Broadcast {
        const val PATH = "/groupChat/broadcast"
        
        /**
         * 消息类型枚举
         */
        object MessageType {
            const val TEXT = 1 // 文字消息
            const val IMAGE = 2 // 图片消息
            const val FILE = 3 // 文件消息
        }
        
        /**
         * 消息状态枚举
         */
        object Status {
            const val SUCCESS = "success"
            const val ERROR = "error"
        }
        
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val content: String? = null, // 可选，消息内容
            val messageType: Int = MessageType.TEXT, // 可选，消息类型，默认值: 1
            val messageId: Long? = null, // 可选，消息ID（发送后返回）
            val timestamp: Long? = null, // 可选，发送时间戳
            val status: String? = null, // 可选，消息状态
            val errorMsg: String? = null // 可选，错误信息（如果发送失败）
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int? = null,
            val message: String? = null,
            val data: Any? = null // 通常为null
        )
    }
}