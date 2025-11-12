package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.mine.databinding.ActivityAiCreateHomeworkBinding
import com.jxdx.mine.http.ApiService
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiCreateHomeworkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiCreateHomeworkBinding
    private var subjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiCreateHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传入的subjectId
        subjectId = intent.getIntExtra("subjectId", 1)
        
        // 设置键盘模式，防止底部按钮被键盘推上去
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        initViews()
        setupListeners()
    }

    private fun initViews() {
        // 设置返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // 取消按钮
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // AI创建按钮
        binding.btnCreate.setOnClickListener {
            createHomeworkWithAi()
        }
    }

    private fun createHomeworkWithAi() {
        val prompt = binding.etPrompt.text.toString().trim()
        
        if (prompt.isEmpty()) {
            Toast.makeText(this, "请输入您的需求", Toast.LENGTH_SHORT).show()
            return
        }

        if (prompt.length < 3) {
            Toast.makeText(this, "请输入更详细的需求", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载状态
        binding.btnCreate.isEnabled = false
        binding.btnCreate.text = "AI创建中..."

        // 调用AI创建作业API
        RetrofitClient.apiService.createHomeworkWithAi(
            msg = prompt,
            subjectId = subjectId ?: 1
        ).enqueue(object : Callback<com.example.corekit.http.bean.BaseResp<String>> {
            override fun onResponse(
                call: Call<com.example.corekit.http.bean.BaseResp<String>>,
                response: Response<com.example.corekit.http.bean.BaseResp<String>>
            ) {
                binding.btnCreate.isEnabled = true
                binding.btnCreate.text = "AI创建"
                
                if (response.isSuccessful && response.body()?.code == 0) {
                    Toast.makeText(this@AiCreateHomeworkActivity, "AI创建作业成功！", Toast.LENGTH_SHORT).show()
                    // 返回成功结果
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: "AI创建失败"
                    Toast.makeText(this@AiCreateHomeworkActivity, "AI创建失败: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(
                call: Call<com.example.corekit.http.bean.BaseResp<String>>,
                t: Throwable
            ) {
                binding.btnCreate.isEnabled = true
                binding.btnCreate.text = "AI创建"
                Toast.makeText(this@AiCreateHomeworkActivity, "网络异常，AI创建失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
