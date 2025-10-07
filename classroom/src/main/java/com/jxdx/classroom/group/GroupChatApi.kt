package com.jxdx.classroom.group

object GroupChatApi {
    /**
     * 获取小组历史消息接口
     */
    object GetHistory {
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
     */
    object SaveMessage {
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val teamId: Int? = null, // 可选，团队ID
            val fromUserId: Int? = null, // 可选，发送用户ID
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
    
    /**
     * 发送消息接口
     */
    object SendMessage {
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val teamId: Int? = null, // 可选，团队ID
            val fromUserId: Int? = null, // 可选，发送用户ID
            val content: String? = null, // 可选，消息内容
            val messageType: Int? = null // 可选，消息类型
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int? = null,
            val message: String? = null,
            val data: Int? = null // 消息ID
        )
    }
    
    /**
     * 老师统一广播给每个小组接口
     */
    object Broadcast {
        /**
         * 消息类型枚举
         */
        object MessageType {
            const val TEXT = 1 // 文字消息
            const val IMAGE = 2 // 图片消息
            const val FILE = 3 // 文件消息
        }
        
        /**
         * 请求参数 (Body 参数 application/json)
         */
        data class RequestBody(
            val teamId: List<Int>? = null, // 可选，团队ID数组
            val fromUserId: Int? = null, // 可选，发送用户ID
            val content: String? = null, // 可选，消息内容
            val messageType: Int? = null // 可选，消息类型
        )
        
        /**
         * 响应数据
         */
        data class Response(
            val code: Int? = null,
            val message: String? = null,
            val data: List<Int>?
        )
    }
}