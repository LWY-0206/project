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
import com.jxdx.mine.http.request.EditHomeworkRequest
import com.jxdx.mine.http.request.CreateHomeworkRequest
import com.jxdx.mine.http.request.SubmitHomeworkRequest
import com.jxdx.mine.http.vo.TeachCreateHWSimpleVO
import com.jxdx.mine.http.vo.TeachCreateHWDetailVO
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // 获取老师对应学科的上课班级
    @GET("/api/teacher/subject/class")
    fun getTeacherSubjectClass(
        @Query("subjectId") subjectId: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<List<ClassInfo>>>
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


    //未完成作业的分页查询（未提交）
    @GET("/api/stu/homework")
    fun getHomework(
        @Query ("subjectId") subjectId: Int?,
        @Query ("page") page: Int,
        @Query ("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<Homework>>>
    //查看该学科已完成但未批改的作业（待批改）
    @GET("/api/stu/homework/cmpl/uncor")
    fun getHomeworkCmpl(
        @Query ("subjectId") subjectId: Int?,
        @Query ("page") page: Int,
        @Query ("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<Homework>>>
    //查看该学科已完成并已批改的作业（已完成）
    @GET("/api/stu/homework/cmpl/cor")
    fun getHomeworkCmplcor(
        @Query ("subjectId") subjectId: Int?,
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

    // 上传文件
    @Multipart
    @POST("/common/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<BaseResp<List<String>>>

    //上传课件
    @POST("/api/teacher/courses/uploadFile")
    fun uploadCourseWare(
        @Body request: UploadCourseWareRequest,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<String>>



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

    //获取已发布的作业列表
    @GET("/api/teach/homework/send/list")
    fun getPublishedHomeworkList(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<PageData<TeachCreateHWSimpleVO>>>

    //获取已发布作业详情
    @GET("/api/teach/homework/send/find")
    fun getPublishedHomeworkDetail(
        @Query("homeworkId") homeworkId: Long,
        @Header ("satoken") satoken: String? = TokenManager.getToken() ?: ""
    ): Call<BaseResp<TeachCreateHWDetailVO>>
}





//上传课件请求类
data class UploadCourseWareRequest(
    val subjectId: Int? = null,
    val fileUrl: String? = null,
    val fileDescription: String? = null,
    val classIds: List<Int>? = null
)

// 班级信息数据类
data class ClassInfo(
    val classId: Int? = null,
    val className: String? = null
)



































