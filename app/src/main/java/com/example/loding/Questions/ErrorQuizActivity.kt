package com.example.loding.Questions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.example.corekit.http.bean.Resource
import com.example.loding.R
import com.example.loding.databinding.ActivityErrorQuizBinding

class ErrorQuizActivity : BaseActivity<ActivityErrorQuizBinding>() {

    private lateinit var viewModel: ErrorQuizViewModel
    private var currentQuestionIndex = 0
    private lateinit var currentQuestions: List<ErrorQuizItem>
    private var filteredQuestions: List<ErrorQuizItem> = emptyList()
    private var currentPage = 1
    private var totalPages = 1
    private val pageSize = 8
    private var subjectId = 1 // 默认科目ID
    private var selectedQuestionType: QuestionType? = null

    override fun bindLayout(): ActivityErrorQuizBinding {
        return ActivityErrorQuizBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取传递的题型参数
        val questionTypeName = intent.getStringExtra("QUESTION_TYPE")
        selectedQuestionType = if (questionTypeName != null) {
            QuestionType.valueOf(questionTypeName)
        } else {
            null
        }

        // 设置题型标题
        view.tvQuestionType.text = getQuestionTypeTitle(selectedQuestionType)
    }

    override fun initView() {
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[ErrorQuizViewModel::class.java]

        // 设置按钮监听器
        setupButtonListeners()

        // 加载错题
        loadErrorQuestions()
    }

    override fun subscribeUi() {
        viewModel.errorQuizLiveData.observe(this) { resource ->
            // 使用 Resource 类提供的 onSuccess 和 onError 方法
            resource
                .onSuccess { data ->
                    // 成功状态处理
                    view.progressBar.visibility = View.GONE
                    data?.let { quizData ->
                        currentQuestions = quizData.records
                        totalPages = quizData.pages
                        currentPage = quizData.current

                        // 应用筛选
                        applyQuestionFilter()

                        updatePageInfo()
                        if (filteredQuestions.isNotEmpty()) {
                            displayQuestion(0)
                        } else {
                            showNoQuestionsMessage()
                        }
                        Log.d("ErrorQuizActivity", "数据加载成功，共${currentQuestions.size}道题目，筛选后${filteredQuestions.size}道")
                    } ?: run {
                        Log.w("ErrorQuizActivity", "数据加载成功但data为null")
                        Toast.makeText(this, "未获取到题目数据", Toast.LENGTH_SHORT).show()
                    }
                }
                .onError { error, data ->
                    // 错误状态处理
                    view.progressBar.visibility = View.GONE

                    // 检查是否有数据在错误响应中
                    if (data != null) {
                        // 即使请求状态是错误，但如果有数据，仍然显示
                        currentQuestions = data.records
                        totalPages = data.pages
                        currentPage = data.current

                        // 应用筛选
                        applyQuestionFilter()

                        updatePageInfo()
                        if (filteredQuestions.isNotEmpty()) {
                            displayQuestion(0)
                        } else {
                            showNoQuestionsMessage()
                        }
                        Toast.makeText(this, "注意: ${error?.message}", Toast.LENGTH_SHORT).show()
                        Log.w("ErrorQuizActivity", "数据加载异常但仍有数据: ${error?.message}")
                    } else {
                        Toast.makeText(this, "加载失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ErrorQuizActivity", "数据加载失败: $error")
                    }
                }
        }
    }

    private fun getQuestionTypeTitle(questionType: QuestionType?): String {
        return when (questionType) {
            QuestionType.SINGLE_CHOICE -> "单选题错题"
            QuestionType.MULTIPLE_CHOICE -> "多选题错题"
            QuestionType.TRUE_FALSE -> "判断题错题"
            QuestionType.FILL_BLANK -> "填空题错题"
            else -> "全部错题"
        }
    }

    private fun setupButtonListeners() {
        view.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1)
            } else if (currentPage > 1) {
                // 加载上一页
                loadPreviousPage()
            }
        }

        view.btnNext.setOnClickListener {
            if (currentQuestionIndex < filteredQuestions.size - 1) {
                displayQuestion(currentQuestionIndex + 1)
            } else {
                // 加载下一页
                loadNextPage()
            }
        }

        view.btnShowAnswer.setOnClickListener {
            showCorrectAnswer()
        }

        view.btnBackToSelection.setOnClickListener {
            // 返回题型选择页面
            startActivity(Intent(this, QuestionTypeSelectionActivity::class.java))
            finish()
        }
    }

    private fun loadErrorQuestions() {
        // 显示加载进度条
        view.progressBar.visibility = View.VISIBLE

        viewModel.getErrorQuestions(subjectId, currentPage, pageSize)
    }

    private fun loadNextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadErrorQuestions()
        } else {
            Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPreviousPage() {
        if (currentPage > 1) {
            currentPage--
            loadErrorQuestions()
        } else {
            Toast.makeText(this, "已经是第一页了", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyQuestionFilter() {
        filteredQuestions = if (selectedQuestionType == null) {
            currentQuestions
        } else {
            currentQuestions.filter { it.getQuestionType() == selectedQuestionType }
        }
    }

    private fun showNoQuestionsMessage() {
        view.noQuestionsView.visibility = View.VISIBLE
        view.noQuestionsText.text = if (selectedQuestionType == null) {
            "暂无错题"
        } else {
            "暂无${getQuestionTypeName(selectedQuestionType)}错题"
        }
    }

    private fun getQuestionTypeName(type: QuestionType?): String {
        return when (type) {
            QuestionType.SINGLE_CHOICE -> "单选题"
            QuestionType.MULTIPLE_CHOICE -> "多选题"
            QuestionType.TRUE_FALSE -> "判断题"
            QuestionType.FILL_BLANK -> "填空题"
            else -> "所有"
        }
    }

    private fun displayQuestion(index: Int) {
        if (filteredQuestions.isEmpty()) {
            Log.w("ErrorQuizActivity", "filteredQuestions为空，无法显示题目")
            return
        }

        currentQuestionIndex = index
        val question = filteredQuestions[index]

        // 显示题目容器，隐藏无题目提示
        view.noQuestionsView.visibility = View.GONE

        // 更新进度显示
        view.tvProgress.text = "${index + 1}/${filteredQuestions.size}"
        view.tvPageInfo.text = "第${currentPage}页/共${totalPages}页"

        // 设置题目内容
        view.tvQuestionContent.text = question.questionText

        // 清空选项容器
        view.optionsContainer.removeAllViews()

        // 根据题目类型创建选项视图
        when (question.getQuestionType()) {
            QuestionType.SINGLE_CHOICE -> createSingleChoiceOptions(question)
            QuestionType.MULTIPLE_CHOICE -> createMultipleChoiceOptions(question)
            QuestionType.TRUE_FALSE -> createTrueFalseOptions(question)
            QuestionType.FILL_BLANK -> createFillBlankOptions(question)
            else -> createUnknownTypeOptions(question)
        }
        // 隐藏正确答案
        view.tvCorrectAnswer.visibility = View.GONE

        // 更新按钮状态
        updateButtonStates()

        Log.d("ErrorQuizActivity", "显示第${index + 1}题: ${question.questionText}")
    }

    private fun createSingleChoiceOptions(question: ErrorQuizItem) {
        val radioGroup = RadioGroup(this)

        val options = listOf(
            question.optionA,
            question.optionB,
            question.optionC,
            question.optionD
        ).filter { it.isNotBlank() }

        options.forEachIndexed { index, optionText ->
            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.tag = index
            radioButton.text = optionText
            radioButton.textSize = 16f
            radioButton.setPadding(16, 16, 16, 16)
            radioButton.setLineSpacing(1.2f, 1.2f)
            radioGroup.addView(radioButton)
        }

        view.optionsContainer.addView(radioGroup)
    }

    private fun createMultipleChoiceOptions(question: ErrorQuizItem) {
        question.options.forEachIndexed { index, optionText ->
            val checkBox = CheckBox(this)
            checkBox.id = View.generateViewId()
            checkBox.tag = index
            checkBox.text = optionText
            checkBox.textSize = 16f
            checkBox.setPadding(16, 16, 16, 16)
            checkBox.setLineSpacing(1.2f, 1.2f)
            view.optionsContainer.addView(checkBox)
        }
    }

    private fun createTrueFalseOptions(question: ErrorQuizItem) {
        question.options.forEachIndexed { index, optionText ->
            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.tag = index
            radioButton.text = optionText
            radioButton.textSize = 16f
            radioButton.setPadding(16, 16, 16, 16)
            radioButton.setLineSpacing(1.2f, 1.2f)
            view.optionsContainer.addView(radioButton)
        }
    }

    private fun createFillBlankOptions(question: ErrorQuizItem) {
        val editText = EditText(this)
        editText.hint = "请输入答案"
        editText.textSize = 16f
        editText.setPadding(16, 16, 16, 16)
        view.optionsContainer.addView(editText)
    }

    private fun createUnknownTypeOptions(question: ErrorQuizItem) {
        val textView = TextView(this)
        textView.text = "未知题型，无法显示选项"
        textView.textSize = 16f
        textView.setPadding(16, 16, 16, 16)
        view.optionsContainer.addView(textView)
    }
    private fun updateButtonStates() {
        view.btnPrevious.isEnabled = currentQuestionIndex > 0 || currentPage > 1
        view.btnNext.isEnabled = currentQuestionIndex < filteredQuestions.size - 1 || currentPage < totalPages
    }

    private fun updatePageInfo() {
        view.tvPageInfo.text = "第${currentPage}页/共${totalPages}页"
    }

    private fun showCorrectAnswer() {
        if (filteredQuestions.isEmpty()) return

        val question = filteredQuestions[currentQuestionIndex]
        view.tvCorrectAnswer.text = "正确答案: ${question.correctOption}"
        view.tvCorrectAnswer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ErrorQuizActivity", "Activity销毁")
    }
}