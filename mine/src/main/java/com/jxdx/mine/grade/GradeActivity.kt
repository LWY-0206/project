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
        list.add(Member("t2", "李老师", "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/Jr98/458X300/92.jpg", "数学老师", 1))

        // Section: 学生
        list.add(Member("section_student", "学生", "","",0))
        list.add(Member("s1", "王小明", "https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/b7c3d5e8-f9a2-4b3c-d4e5-f6a7b8c9d0e1.jpg", "",2))
        list.add(Member("s2", "李华", "https://example.com/student2.png","",2))
        list.add(Member("s3", "赵六", "https://example.com/student2.png", "",2))
        // 添加更多学生数据，使用实际网络图片URL
        list.add(Member("s4", "陈晨", "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/Jr96/458X300/90.jpg", "",2))
        list.add(Member("s5", "刘芳", "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/8dadc437-9f67-412c-b057-6a902b25e438.png", "",2))
        list.add(Member("s6", "张伟", "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/f97fba60-a22b-431d-a55b-a5c182963bc5.png", "",2))
        list.add(Member("s7", "王丽", "https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/a0bf2736-b843-4505-ad82-36c38a925f7c.jpg", "",2))
        // 继续添加更多学生数据
        list.add(Member("s8", "刘伟", "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/Jr97/458X300/91.jpg", "",2))
        list.add(Member("s9", "张明", "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/425f8c39-7a21-4f3e-b2c1-8d7e3a92b45f.png", "",2))
        list.add(Member("s10", "李娜", "https://classroom-interaction.oss-cn-hangzhou.aliyuncs.com/updateFiles/a63b9d24-5f1d-4e2c-a8b7-9c5d2e8f1a3b.png", "",2))
        list.add(Member("s11", "赵阳", "https://tongue-srt.oss-cn-hangzhou.aliyuncs.com/2025/09/05/b7c3d5e8-f9a2-4b3c-d4e5-f6a7b8c9d0e1.jpg", "",2))
        list.add(Member("s12", "黄琳", "https://tc-new.z.wiki/autoupload/f/d9oSIkypaT4MX13ceI-M6PmYtDrGvPpsluM_NdUVaNGyl5f0KlZfm6UsKj-HyTuv/20250905/Jr98/458X300/92.jpg", "",2))


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
