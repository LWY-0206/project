package com.jxdx.mine.grade

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.Member
import com.jxdx.mine.R
import com.jxdx.mine.UserInfo
import com.jxdx.mine.adapter.ClassMemberAdapter
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GradeActivity : AppCompatActivity() {

    private lateinit var adapter: ClassMemberAdapter
    private var graded: String="软件222"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        val tvClassName = findViewById<TextView>(R.id.tv_class_name)
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_member_list)
        
        // 设置返回按钮监听
        ivBack.setOnClickListener { finish() }
        
        // 初始化RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ClassMemberAdapter { member ->
            Toast.makeText(this, "点击：${member.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
        
        // 加载模拟数据
        loadMockData()
        
        // 更新班级信息
        updateUseGrade(tvClassName)
    }

    private fun loadMockData() {
        val list = mutableListOf<Member>()

        // Section: 老师
        list.add(Member("section_teacher", "老师", "","",0))
        list.add(Member("t1", "张老师", "https://example.com/teacher1.png", "班主任", 1))
        list.add(Member("t2", "李老师", "https://example.com/teacher2.png", "数学老师", 1))

        // Section: 学生
        list.add(Member("section_student", "学生", "","",0))
        list.add(Member("s1", "王小明", "https://example.com/student1.png", "",2))
        list.add(Member("s2", "李华", "https://example.com/student2.png","",2))
        list.add(Member("s3", "赵六", "https://example.com/student2.png", "",2))

        adapter.submitList(list)
    }
    
    private fun updateUseGrade(tvClassName: TextView) {
        tvClassName.text = "加载中..."
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(
                call: Call<BaseResp<UserInfo>?>,
                response: Response<BaseResp<UserInfo>?>?
            ) {
                if (response?.isSuccessful == true) {
                    response.body()?.let {
                        if (it.code == 0 && it.data != null) {
                            // 安全地更新班级名称
                            graded = it.data!!.className
                            tvClassName.text = graded
                            Log.d("GradeActivity", "成功获取班级信息: $graded")
                        } else {
                            // 保持默认值
                            tvClassName.text = graded
                        }
                    }
                }
            }
            override fun onFailure(
                call: Call<BaseResp<UserInfo>?>,
                t: Throwable
            ) {
                // 显示默认班级名称
                findViewById<TextView>(R.id.tv_class_name).text = graded
            }
        })
    }
}
