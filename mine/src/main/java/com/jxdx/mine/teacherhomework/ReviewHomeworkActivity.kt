package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.mine.databinding.ActivityReviewHomeworkBinding

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

            // 模拟提交到后端
            // 实际项目中，这里应该调用API将批改结果保存到服务器
            Toast.makeText(this, "已保存批改结果", Toast.LENGTH_SHORT).show()
            
            // 返回上一页并传递批改结果
            val resultIntent = Intent()
            resultIntent.putExtra("score", score)
            resultIntent.putExtra("comment", comment)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}