package com.jxdx.resource.Questions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.databinding.ActivityQuestionSelectionBinding

class QuestionSelectionActivity : BaseActivity<ActivityQuestionSelectionBinding>() {
    private lateinit var binding: ActivityQuestionSelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun bindLayout(): ActivityQuestionSelectionBinding {
        binding = ActivityQuestionSelectionBinding.inflate(layoutInflater)
        return binding
    }

    override fun initView() {
        setupClickListeners()
    }

    override fun subscribeUi() {

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

    // 在 QuestionSelectionActivity.kt 中修改 navigateToErrorQuiz 方法
    private fun navigateToErrorQuiz(questionType: QuestionType?) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("QUESTION_TYPE", questionType?.name ?: QuestionType.COMPREHENSIVE.name)
        }
        startActivity(intent)
        finish() // 结束选择页面
    }
}