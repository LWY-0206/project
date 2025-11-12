package com.example.loding.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.entrance.EntranceActivity
import com.jxdx.home.HomeActivity
import com.jxdx.home.TeacherHomeActivity
import com.jxdx.login.Login.LoginActivity
import com.jxdx.login.Login.RetrofitClient
import com.jxdx.login.UserInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var identity: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查登录状态
        val preferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn: Boolean = preferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
                override fun onResponse(
                    call: Call<BaseResp<UserInfo>?>,
                    response: Response<BaseResp<UserInfo>?>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.code == 0) {
                                identity = it.data?.identity ?: 0
                            } else {
                                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    else{
                        Toast.makeText(
                            this@MainActivity,
                            "获取用户信息失败，错误: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<BaseResp<UserInfo>?>,
                    t: Throwable
                ) {
                    Toast.makeText(this@MainActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()

                }
            })
            if(identity==0){//学生
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, TeacherHomeActivity::class.java)
                startActivity(intent)
                finish()
            }

        } else {
            // 未登录，跳转到登录页
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}