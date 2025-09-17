package com.jxdx.square.friend

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.square.entity.UserInfo
import com.jxdx.square.entity.ApplicationMessage
import com.jxdx.square.entity.Friend
class FriendRepository {
    private val service: FriendApi by lazy {
        HttpManager.instance.service(FriendApi::class.java)
    }

    suspend fun getFriends(): BaseResp<List<Friend>> = service.getFriends("6a819474-1cf4-42b2-b12e-794c4b472820")

    suspend fun getUserInfo(userId: String): BaseResp<UserInfo> = service.getUserInfo("6a819474-1cf4-42b2-b12e-794c4b472820", userId)

    suspend fun addFriend(applicationBody: ApplicationBody): BaseResp<String> =
        service.addFriend("6a819474-1cf4-42b2-b12e-794c4b472820", applicationBody)

    suspend fun getApplications(): BaseResp<List<ApplicationMessage>> = service.getApplications("6a819474-1cf4-42b2-b12e-794c4b472820")
}
