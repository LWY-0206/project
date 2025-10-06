package com.jxdx.classroom.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.AllCourse
import com.jxdx.classroom.UserInfo
import com.jxdx.classroom.com.jxdx.classroom.http.DTO.FreeDistribution
import com.jxdx.classroom.com.jxdx.classroom.http.DTO.GenerateGroupDTO
import com.jxdx.classroom.com.jxdx.classroom.http.DTO.OutStuDTO
import com.jxdx.classroom.http.DTO.JoinStuDTO
import com.jxdx.classroom.entity.ClassLive
import com.jxdx.classroom.entity.Classroom
import com.jxdx.classroom.entity.CreateLiveRoomRequest
import com.jxdx.classroom.entity.RtmpUrl
import com.jxdx.classroom.entity.SelectClass
import com.jxdx.classroom.group.Group
import com.jxdx.classroom.group.Student
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //获取当前用户信息
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>

    //查看所有课程
    @GET("/api/student/courses/list")
    fun getAllCourse(
        @Header("satoken")satoken: String= TokenManager.getToken()?:""): Call<BaseResp<List<AllCourse>>>

    @GET("/live/room/student/page")
    suspend fun getClassRoom(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): BaseResp<ArrayList<ClassLive>>

    @GET("/api/teacher/subject")
    suspend fun getSubject(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): BaseResp<ArrayList<SelectClass>>

    @GET("/live/room/detail/{liveId}")
    suspend fun getRtmpUrl(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Path("liveId") liveId: Int
    ): BaseResp<RtmpUrl>

    @GET("/api/teacher/subject/class")
    suspend fun getClassRoom(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Query("subjectId") subjectId: Int
    ): BaseResp<ArrayList<Classroom>>

    @POST("/live/room/teacher/create")
    suspend fun createLiveRoom(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: CreateLiveRoomRequest
    ): BaseResp<Int>

    @GET("/live/room/teacher/stream/key/{liveId}")
    suspend fun getStreamKey(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Path("liveId") liveId: Int
    ): BaseResp<String>



// 生成小组
    @POST("/group/generate/groups")
    suspend fun generateGroups(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: GenerateGroupDTO
    ): BaseResp<Unit>
//学生加入
    @POST("/group/join/free")
    suspend fun joinGroups(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: JoinStuDTO
    ): BaseResp<Unit>
//学生退出
    @POST("/group/quit")
    suspend fun outGroups(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
       @Body request: OutStuDTO
    ): BaseResp<Unit>
//获取小组列表
    @GET("/group/member/list")
    suspend fun getGroups(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Query("subjectId") subjectId: Int,
        @Query("createdBy") createdBy:Int
    ): BaseResp<MutableList<Group>>

//自由分配未入组成员
    @POST("/group/free/assign")
    suspend fun freeDistribution(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: FreeDistribution
    ): BaseResp<Unit>

//结束分组
    @POST("/group/end")
    suspend fun endGroup(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Query("subjectId") subjectId:Int,
        @Query("createdBy") createdBy:Int,
    ): BaseResp<Unit>

    // 小组聊天相关API
    // 获取历史消息
    @GET("/groupChat/getHistory")
    suspend fun getGroupChatHistory(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Query("teamId") teamId: Int,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResp<com.jxdx.classroom.group.GroupChatApi.GetHistory.Response.Data>

    // 保存消息
    @POST("/groupChat/saveMessage")
    suspend fun saveGroupChatMessage(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: com.jxdx.classroom.group.GroupChatApi.SaveMessage.RequestBody
    ): BaseResp<Int>

    // 发送消息
    @POST("/groupChat/sendMessage")
    suspend fun sendGroupChatMessage(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: com.jxdx.classroom.group.GroupChatApi.SendMessage.RequestBody
    ): BaseResp<Long>

    // 教师广播消息
    @POST("/groupChat/broadcast")
    suspend fun broadcastGroupChatMessage(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: "",
        @Body request: com.jxdx.classroom.group.GroupChatApi.Broadcast.RequestBody
    ): BaseResp<Any>
}