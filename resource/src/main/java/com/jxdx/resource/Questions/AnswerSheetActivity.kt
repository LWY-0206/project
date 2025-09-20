package com.jxdx.resource.Questions

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.jxdx.resource.adpter.AnswerSheetAdapter
import com.jxdx.resource.databinding.ActivityAnswerSheetBinding

class AnswerSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnswerSheetBinding
    private lateinit var questions: List<ErrorQuizItem>
    private lateinit var userAnswers: Map<Int, String>
    private lateinit var answerResults: List<Boolean>
    private var currentQuestionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的数据
        questions = intent.getParcelableArrayListExtra("questions") ?: emptyList()
        userAnswers = intent.getSerializableExtra("user_answers") as? Map<Int, String> ?: emptyMap()
        answerResults = intent.getBooleanArrayExtra("answer_results")?.toList() ?: emptyList()
        currentQuestionIndex = intent.getIntExtra("current_index", 0)

        // 设置正确率统计
        setupStatistics()

        // 设置答题卡
        setupAnswerSheet()

        // 设置按钮监听器
        setupButtonListeners()
    }

    private fun setupStatistics() {
        val totalQuestions = questions.size
        val correctCount = answerResults.count { it }
        val incorrectCount = answerResults.withIndex().count { (index, isCorrect) ->
            !isCorrect && userAnswers.containsKey(index)
        }

        val unansweredCount = totalQuestions - userAnswers.size

        val accuracy = if (totalQuestions > 0) {
            correctCount * 100f / totalQuestions
        } else {
            0f
        }

        binding.tvAccuracy.text = "${"%.1f".format(accuracy)}%"
        binding.tvCorrectCount.text = correctCount.toString()
        binding.tvIncorrectCount.text = incorrectCount.toString()
        binding.tvUnansweredCount.text = unansweredCount.toString()
    }

    private fun setupAnswerSheet() {
        val layoutManager = GridLayoutManager(this, 5)
        binding.rvAnswerSheet.layoutManager = layoutManager

        val adapter = AnswerSheetAdapter(questions, answerResults, userAnswers) { position ->
            // 点击答题卡项，返回QuizActivity并跳转到对应题目
            val resultIntent = Intent().apply {
                putExtra("question_index", position)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        binding.rvAnswerSheet.adapter = adapter
    }

    private fun setupButtonListeners() {
        binding.btnBackToQuiz.setOnClickListener {
            // 返回QuizActivity，继续答题
            val resultIntent = Intent().apply {
                putExtra("question_index", currentQuestionIndex)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.btnFinish.setOnClickListener {
            // 完成答题，返回题型选择页面
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}