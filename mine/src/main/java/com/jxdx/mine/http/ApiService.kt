package com.jxdx.mine.http

import com.example.corekit.http.TokenManager
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.Course
import com.jxdx.mine.CourseDetail
import com.jxdx.mine.Homework
import com.jxdx.mine.PageData
import com.jxdx.mine.StuHomeWorkDetailVO
import com.jxdx.mine.SubjectsVO
import com.jxdx.mine.UserInfo
import com.jxdx.mine.http.request.SubmitHomeworkRequest
import com.jxdx.mine.http.request.CreateHomeworkRequest
import com.jxdx.mine.http.request.EditHomeworkRequest
import com.jxdx.mine.http.vo.TeachCreateHWSimpleVO
import com.jxdx.mine.http.vo.TeachCreateHWDetailVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //根据ID获取用户信息
    @GET("/user/info/{userId}")
    fun getUserInfo(@Path("userId") userId: Int): Call<BaseResp<UserInfo>>
    @GET("/user/info")
    fun getUserInfo(
        @Header ("satoken") satoken: String= TokenManager.getToken() ?: ""
    ): Call<BaseResp<UserInfo>>
    @POST("/user/updateProfile")
    fun updateProfile(
        @Body profile: String,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>

    @GET("/api/stu/homework")
    fun getHomework(
        @Query ("page") page: Int,
        @Query ("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<Homework>>>

    //查看所有课程
    @GET("/api/student/courses/list")
    fun getAllCourse(
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<List<Course>>>

    //获取课程详情
    @GET("/api/student/courses/detail")
    fun getCourseDetail(
        @Query("subjectId") subjectId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<CourseDetail>>




    //获取作业详情
    @GET("/api/stu/homework/detail")
    fun getHomeworkDetail(
        @Query("homeworkId") homeworkId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<StuHomeWorkDetailVO>>
    
    //提交作业
    @POST("/api/stu/homework/submit")
    fun submitHomework(
        @Body submitRequest: SubmitHomeworkRequest,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
    
    //图片上传接口
    @Multipart
    @POST("/common/upload")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<List<String>>>
    
    //老师创建作业
    @POST("/api/teach/homework/create")
    fun createHomework(
        @Body request: CreateHomeworkRequest,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
    
    //老师编辑作业
    @POST("/api/teach/homework/create/edit")
    fun editHomework(
        @Body request: EditHomeworkRequest,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
    
    //老师获取作业列表
    @GET("/api/teach/homework/create/list")
    fun getTeacherHomeworkList(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>    
    
    //老师查看作业详情
    @GET("/api/teach/homework/create/find")
    fun getTeacherHomeworkDetail(
        @Query("homeworkId") homeworkId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<TeachCreateHWDetailVO>>
    
    //老师批改作业并保存结果
    @POST("/api/teach/homework/create/review")
    fun submitHomeworkReview(
        @Query("homeworkId") homeworkId: Long,
        @Query("studentId") studentId: Long,
        @Query("score") score: Int,
        @Query("comment") comment: String,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<Any>>
    
    //获取老师对应的学科列表
    @GET("/api/teacher/subject")
    fun getTeacherSubject(
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<List<SubjectsVO>>>
    
    //删除作业（支持批量删除）
    @DELETE("/api/teach/homework/send/del")
    fun deleteHomework(
        @Query("homeworkId") homeworkId: String,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
    
    //AI创建作业
    @POST("/api/teach/homework/create/ai")
    fun createHomeworkWithAi(
        @Query("msg") msg: String,
        @Query("subjectId") subjectId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
    
    //发布作业
    @POST("/api/teach/homework/send")
    fun publishHomework(
        @Query("homeworkId") homeworkId: String,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>
}





































