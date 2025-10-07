package com.jxdx.square.message

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.jxdx.square.entity.MessageItem
import com.jxdx.square.entity.UserInfo
import com.jxdx.square.friend.FriendRepository

class MessageViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val friendRepository: FriendRepository by lazy {
        FriendRepository()
    }
    
    val messageListLiveData: ResLiveData<List<MessageItem>> by lazy {
        ResLiveData()
    }

    fun loadChatFriends() {
        request(
            ResLiveData<List<Int>>(),
            object : LiveDataCallback<List<Int>, List<Int>> {
                override fun success(
                    emit: ResLiveData<List<Int>>,
                    msg: String?,
                    data: List<Int>?,
                ) {
                    data?.let { friendIds ->
                        // 获取好友ID列表后，需要获取每个好友的详细信息
                        loadFriendDetails(friendIds)
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<Int>>,
                    code: Int?,
                    msg: String?,
                    data: List<Int>?,
                ) {
                    messageListLiveData.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<Int>>,
                    e: ErrorResponse,
                ) {
                    messageListLiveData.error(e, null)
                }
            },
            {
                friendRepository.getChatFriends()
            },
        )
    }

    private fun loadFriendDetails(friendIds: List<Int>) {
        val messageItems = mutableListOf<MessageItem>()
        var completedCount = 0
        val totalCount = friendIds.size

        if (totalCount == 0) {
            messageListLiveData.success(emptyList())
            return
        }

        friendIds.forEach { friendId ->
            request(
                ResLiveData<UserInfo>(),
                object : LiveDataCallback<UserInfo, UserInfo> {
                    override fun success(
                        emit: ResLiveData<UserInfo>,
                        msg: String?,
                        data: UserInfo?,
                    ) {
                        data?.let { userInfo ->
                            val messageItem = MessageItem(
                                avatarResId = 0, // 使用默认头像
                                name = userInfo.userName,
                                time = "刚刚", // 暂时使用固定时间，后续可以从消息接口获取
                                message = "点击开始聊天", // 暂时使用固定消息，后续可以从消息接口获取
                                friendId = friendId,
                                avatarUrl = userInfo.avatarUrl
                            )
                            messageItems.add(messageItem)
                        }
                        
                        completedCount++
                        if (completedCount == totalCount) {
                            messageListLiveData.success(messageItems)
                        }
                    }

                    override fun otherCode(
                        emit: ResLiveData<UserInfo>,
                        code: Int?,
                        msg: String?,
                        data: UserInfo?,
                    ) {
                        Log.w("MessageViewModel", "获取用户信息失败: $msg")
                        completedCount++
                        if (completedCount == totalCount) {
                            messageListLiveData.success(messageItems)
                        }
                    }

                    override fun error(
                        emit: ResLiveData<UserInfo>,
                        e: ErrorResponse,
                    ) {
                        Log.e("MessageViewModel", "获取用户信息出错: ${e.message}")
                        completedCount++
                        if (completedCount == totalCount) {
                            messageListLiveData.success(messageItems)
                        }
                    }
                },
                {
                    friendRepository.getUserInfo(friendId.toString())
                },
            )
        }
    }
}
