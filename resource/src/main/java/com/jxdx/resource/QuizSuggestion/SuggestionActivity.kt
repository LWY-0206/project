package com.jxdx.resource.QuizSuggestion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jxdx.resource.R
import com.jxdx.resource.databinding.ActivitySuggestionBinding

class SuggestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuggestionBinding
    private lateinit var viewModel: SuggestionViewModel
    private var subjectId: Int = 0
    private var questionIds: List<Int> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        subjectId = intent.getIntExtra("subjectId", 0)
        questionIds = intent.getIntegerArrayListExtra("questionIds") ?: emptyList()

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[SuggestionViewModel::class.java]

        // 设置UI
        setupUI()

        // 观察数据
        observeData()

        // 加载建议
        loadSuggestion()
    }

    private fun setupUI() {
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 设置重新测试按钮
        binding.btnRetest.setOnClickListener {
            // 返回到题目选择页面
            finish()
        }

        // 设置查看详情按钮
        binding.btnViewDetails.setOnClickListener {
            // 可以跳转到错题详情页面
            // 这里暂时只是关闭当前页面
            finish()
        }
    }

    private fun observeData() {
        viewModel.SuggesstionLiveData.observe(this) { resource ->
            resource
                .onSuccess { data ->
                    binding.progressBar.visibility = View.GONE
                    data?.let { displaySuggestion(it) }
                }
                .onError { error, _ ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "加载建议失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                    // 显示错误状态
                    binding.errorView.visibility = View.VISIBLE
                    binding.suggestionContent.visibility = View.GONE
                }
        }
    }

    private fun loadSuggestion() {
        if (questionIds.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE
            viewModel.getQuizSuggestion(subjectId, questionIds)
        } else {
            binding.errorView.visibility = View.VISIBLE
            binding.suggestionContent.visibility = View.GONE
            binding.tvError.text = "没有错题数据"
        }
    }

    private fun displaySuggestion(data: SuggestionData) {
        binding.suggestionContent.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE

        // 设置学科名称
        binding.tvSubjectName.text = data.subjectName

        // 设置题目类型
        binding.tvQuestionType.text = data.questionType

        // 设置题目数量
        binding.tvQuestionCount.text = "错题数量: ${data.questionCount}"

        // 清空之前的建议
        binding.suggestionsContainer.removeAllViews()

        // 添加建议
        data.suggestions.forEach { suggestion ->
            val suggestionView = layoutInflater.inflate(
                R.layout.item_suggestion,
                binding.suggestionsContainer,
                false
            ) as TextView

            suggestionView.text = "• $suggestion"
            binding.suggestionsContainer.addView(suggestionView)
        }
    }

    companion object {
        fun start(context: Context, subjectId: Int, questionIds: List<Int>) {
            Log.d("subjectId",subjectId.toString())
            Log.d("questionIds",questionIds.toString())
            val intent = Intent(context, SuggestionActivity::class.java).apply {
                putExtra("subjectId", subjectId)
                putIntegerArrayListExtra("questionIds", ArrayList(questionIds))
            }
            context.startActivity(intent)
        }
    }
}