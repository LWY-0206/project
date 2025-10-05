package com.jxdx.square.friend

import com.example.corekit.http.HttpManager
import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.UserInfo
import com.jxdx.square.entity.ApplicationMessage
import com.jxdx.square.entity.Friend
class FriendRepository {
    private val service: FriendApi by lazy {
        HttpManager.instance.service(FriendApi::class.java)
    }

    suspend fun getFriends(): BaseResp<List<Friend>> = service.getFriends(TokenManager.getToken().toString())

    suspend fun getUserInfo(userId: String): BaseResp<UserInfo> = service.getUserInfo(TokenManager.getToken().toString(), userId)

    suspend fun addFriend(applicationBody: ApplicationBody): BaseResp<String> =
        service.addFriend(TokenManager.getToken().toString(), applicationBody)

    suspend fun getApplications(): BaseResp<List<ApplicationMessage>> = service.getApplications(TokenManager.getToken().toString())

    suspend fun acceptFriend(applicationId: Int): BaseResp<String> = service.acceptFriend(TokenManager.getToken().toString(), applicationId)

    suspend fun rejectFriend(applicationId: Int): BaseResp<String> = service.rejectFriend(TokenManager.getToken().toString(), applicationId)

    suspend fun deleteFriend(friendId: Int): BaseResp<String> = service.deleteFriend(TokenManager.getToken().toString(), friendId)

    suspend fun getChatFriends(): BaseResp<List<Int>> = service.getChatFriends(TokenManager.getToken().toString())
}
