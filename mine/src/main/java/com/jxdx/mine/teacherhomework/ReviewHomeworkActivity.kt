package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.http.bean.BaseResp
import com.jxdx.mine.databinding.ActivityReviewHomeworkBinding
import com.jxdx.mine.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewHomeworkActivity: AppCompatActivity() {
    private lateinit var binding: ActivityReviewHomeworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentName = intent.getStringExtra("studentName")
        val content = intent.getStringExtra("content")
        val oldScore = intent.getIntExtra("score", -1)
        val oldComment = intent.getStringExtra("comment")
        val homeworkId = intent.getStringExtra("homeworkId")
        val studentId = intent.getStringExtra("studentId")

        supportActionBar?.title = "批改 - $studentName"

        binding.tvStudentName.text = studentName
        binding.tvHomeworkContent.text = "作业内容：$content"
        if (oldScore != -1) binding.etScore.setText(oldScore.toString())
        binding.etComment.setText(oldComment)

        binding.btnSaveReview.setOnClickListener {
            val scoreStr = binding.etScore.text.toString()
            val score = scoreStr.toIntOrNull()
            val comment = binding.etComment.text.toString()

            if (scoreStr.isEmpty()) {
                Toast.makeText(this, "请输入分数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (score == null || score < 0 || score > 100) {
                Toast.makeText(this, "请输入0-100之间的有效分数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 显示加载状态
            binding.btnSaveReview.isEnabled = false
            binding.btnSaveReview.text = "保存中..."
            
            // 调用保存批改结果的API
            RetrofitClient.apiService.submitHomeworkReview(
                homeworkId = homeworkId?.toLong() ?: 0,
                studentId = studentId?.toLong() ?: 0,
                score = score,
                comment = comment
            ).enqueue(object : Callback<BaseResp<Any>> {
                override fun onResponse(call: Call<BaseResp<Any>>, response: Response<BaseResp<Any>>) {
                    binding.btnSaveReview.isEnabled = true
                    binding.btnSaveReview.text = "保存批改结果"
                    
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()
                        if (result?.code == 200) {
                            Toast.makeText(this@ReviewHomeworkActivity, "已保存批改结果", Toast.LENGTH_SHORT).show()
                            
                            // 返回上一页并传递批改结果
                            val resultIntent = Intent()
                            resultIntent.putExtra("score", score)
                            resultIntent.putExtra("comment", comment)
                            resultIntent.putExtra("studentId", studentId)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this@ReviewHomeworkActivity, "保存失败: ${result?.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ReviewHomeworkActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(call: Call<BaseResp<Any>>, t: Throwable) {
                    binding.btnSaveReview.isEnabled = true
                    binding.btnSaveReview.text = "保存批改结果"
                    Toast.makeText(this@ReviewHomeworkActivity, "网络异常: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            
            // 如果API调用失败，仍然模拟成功（作为备用方案）
            // val resultIntent = Intent()
            // resultIntent.putExtra("score", score)
            // resultIntent.putExtra("comment", comment)
            // setResult(RESULT_OK, resultIntent)
            // finish()
        }
    }
}