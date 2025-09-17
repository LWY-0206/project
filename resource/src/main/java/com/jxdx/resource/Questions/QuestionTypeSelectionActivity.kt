package com.jxdx.resource.Questions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.resource.databinding.ActivityQuestionTypeSelectionBinding

class QuestionTypeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionTypeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionTypeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 全部错题
        binding.cardAll.setOnClickListener {
            navigateToErrorQuiz(null)
        }

        // 单选题
        binding.cardSingle.setOnClickListener {
            navigateToErrorQuiz(QuestionType.SINGLE_CHOICE)
        }

        // 多选题
        binding.cardMultiple.setOnClickListener {
            navigateToErrorQuiz(QuestionType.MULTIPLE_CHOICE)
        }

        // 判断题
        binding.cardTrueFalse.setOnClickListener {
            navigateToErrorQuiz(QuestionType.TRUE_FALSE)
        }

        // 填空题
        binding.cardFillBlank.setOnClickListener {
            navigateToErrorQuiz(QuestionType.FILL_BLANK)
        }
    }

    private fun navigateToErrorQuiz(questionType: QuestionType?) {
        val intent = Intent(this, ErrorQuizActivity::class.java).apply {
            putExtra("QUESTION_TYPE", questionType?.name)
        }
        startActivity(intent)
        finish() // 结束选择页面
    }
}