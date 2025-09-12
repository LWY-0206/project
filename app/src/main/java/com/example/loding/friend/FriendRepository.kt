package com.example.loding.friend

import com.example.corekit.http.HttpManager
import com.example.corekit.http.bean.BaseResp
import com.example.loding.entity.Friend

class FriendRepository {
    private val service: FriendApi by lazy {
        HttpManager.instance.service(FriendApi::class.java)
    }

    suspend fun getFriends(): BaseResp<List<Friend>> = service.getFriends("e3c55f2f-023d-41ed-876b-d144db691e14")
}
