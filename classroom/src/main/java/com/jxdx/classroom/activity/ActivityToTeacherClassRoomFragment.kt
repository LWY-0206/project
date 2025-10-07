package com.jxdx.classroom.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.TokenManager
import com.jxdx.classroom.R
import com.jxdx.classroom.fragment.TeacherClassRoomFragment
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.classroom.UserInfo
import com.example.corekit.http.bean.BaseResp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityToTeacherClassRoomFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_teacher_class_room_fragment)
        if (savedInstanceState == null) {
            // 先尝试从intent获取teacherId
            var teacherId = intent.getIntExtra("teacherId", 0)
            
            // 如果teacherId为0（无效），则获取当前登录用户的ID作为teacherId
            if (teacherId == 0) {
                getCurrentUserId { userId ->
                    if (userId > 0) {
                        teacherId = userId
                        Log.d("TeacherClassRoom", "使用当前登录用户ID作为teacherId: $teacherId")
                    } else {
                        Log.w("TeacherClassRoom", "无法获取有效teacherId")
                    }
                    
                    // 创建TeacherClassRoomFragment并传递参数
                    val fragment = TeacherClassRoomFragment()
                    val bundle = Bundle()
                    bundle.putInt("teacherId", teacherId)
                    fragment.arguments = bundle
                    
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
            } else {
                // 从intent获取到了有效的teacherId
                Log.d("TeacherClassRoom", "从intent获取到teacherId: $teacherId")
                
                // 创建TeacherClassRoomFragment并传递参数
                val fragment = TeacherClassRoomFragment()
                val bundle = Bundle()
                bundle.putInt("teacherId", teacherId)
                fragment.arguments = bundle
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }
        }
    }
    
    /**
     * 获取当前登录用户的ID
     */
    private fun getCurrentUserId(callback: (Int) -> Unit) {
        // 使用RetrofitClient获取用户信息
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(
                call: Call<BaseResp<UserInfo>>,
                response: Response<BaseResp<UserInfo>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 0) {
                        val data = body.data
                        if (data != null) {
                            // 注意：根据UserInfo模型，用户ID字段是id
                            val userId = data.id
                            callback(userId)
                            return
                        }
                    }
                }
                callback(0) // 获取失败返回0
            }
            
            override fun onFailure(call: Call<BaseResp<UserInfo>>, t: Throwable) {
                Log.e("TeacherClassRoom", "获取用户信息失败", t)
                callback(0) // 获取失败返回0
            }
        })
    }
}