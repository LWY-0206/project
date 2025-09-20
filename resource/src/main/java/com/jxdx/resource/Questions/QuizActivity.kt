package com.jxdx.resource.Questions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.R
import com.jxdx.resource.databinding.ActivityQuizBinding

class QuizActivity : BaseActivity<ActivityQuizBinding>() {

    private lateinit var viewModel: ErrorQuizViewModel
    private var currentQuestionIndex = 0
    private lateinit var currentQuestions: List<ErrorQuizItem>
    private var filteredQuestions: List<ErrorQuizItem> = emptyList()
    private var currentPage = 1
    private var totalPages = 1
    private val pageSize = 10
    private var subjectId = 1
    private var selectedQuestionType: QuestionType? = null
    private val userAnswers = mutableMapOf<Int, String>()
    private val answerResults = mutableListOf<Boolean>()

    // 添加请求码常量
    private companion object {
        const val REQUEST_ANSWER_SHEET = 1001
    }

    override fun bindLayout(): ActivityQuizBinding {
        return ActivityQuizBinding.inflate(layoutInflater)
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
            resource
                .onSuccess { data ->
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
                            initAnswerResults()
                            view.noQuestionsView.visibility = View.GONE
                        } else {
                            showNoQuestionsMessage()
                        }
                    }
                }
                .onError { error, data ->
                    view.progressBar.visibility = View.GONE
                    if (data != null) {
                        currentQuestions = data.records
                        totalPages = data.pages
                        currentPage = data.current

                        applyQuestionFilter()
                        updatePageInfo()
                        if (filteredQuestions.isNotEmpty()) {
                            displayQuestion(0)
                            initAnswerResults()
                            view.noQuestionsView.visibility = View.GONE
                        } else {
                            showNoQuestionsMessage()
                        }
                        Toast.makeText(this, "注意: ${error?.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        showNoQuestionsMessage()
                        Toast.makeText(this, "加载失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initAnswerResults() {
        answerResults.clear()
        userAnswers.clear()
        filteredQuestions.forEach { _ ->
            answerResults.add(false)
        }
    }

    private fun setupButtonListeners() {
        // 返回按钮
        view.btnBack.setOnClickListener {
            onBackPressed()
        }

        // 上一题按钮
        view.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1)
            } else if (currentPage > 1) {
                loadPreviousPage()
            }
        }

        // 下一题按钮
        view.btnNext.setOnClickListener {
            if (currentQuestionIndex < filteredQuestions.size - 1) {
                displayQuestion(currentQuestionIndex + 1)
            } else if (currentPage < totalPages) {
                loadNextPage()
            } else {
                Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show()
            }
        }

        // 提交按钮 - 修改为跳转到答题结果界面
        view.btnSubmit.setOnClickListener {
            view.btnSubmit.text="返回答题卡"
            view.btnSubmit.textSize=12f
            // 提交答案并跳转到答题结果界面
            submitAnswersAndNavigate()
        }

        // 返回选择按钮
        view.btnBackToSelection.setOnClickListener {
            startActivity(Intent(this, QuestionSelectionActivity::class.java))
            finish()
        }
    }

    private fun submitAnswersAndNavigate() {
        // 计算答题结果
        var answeredCount = 0
        var correctCount = 0

        filteredQuestions.forEachIndexed { index, question ->
            val userAnswer = userAnswers[index] ?: ""
            val isCorrect = userAnswer == question.correctOption
            answerResults[index] = isCorrect

            if (userAnswer.isNotEmpty()) {
                answeredCount++
                if (isCorrect) correctCount++
            }
        }

        // 启动答题结果界面
        val intent = Intent(this, AnswerSheetActivity::class.java).apply {
            putParcelableArrayListExtra("questions", ArrayList(filteredQuestions))
            putExtra("user_answers", HashMap(userAnswers))
            putExtra("answer_results", answerResults.toBooleanArray())
            putExtra("current_index", currentQuestionIndex)
        }
        startActivityForResult(intent, REQUEST_ANSWER_SHEET)
    }

    // 处理从答题结果界面返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ANSWER_SHEET) {
            if (resultCode == RESULT_OK) {
                // 从答题结果界面返回，跳转到指定题目
                val questionIndex = data?.getIntExtra("question_index", currentQuestionIndex) ?: currentQuestionIndex
                displayQuestion(questionIndex)
                // 显示正确答案
                showCorrectAnswer()
            } else if (resultCode == RESULT_CANCELED) {
                // 用户选择完成，返回题型选择页面
                startActivity(Intent(this, QuestionSelectionActivity::class.java))
                finish()
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

    private fun loadErrorQuestions() {
        view.progressBar.visibility = View.VISIBLE
        viewModel.getErrorQuestions(subjectId, currentPage, pageSize)
    }

    private fun loadNextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadErrorQuestions()
        }
    }

    private fun loadPreviousPage() {
        if (currentPage > 1) {
            currentPage--
            loadErrorQuestions()
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
        view.tvNoQuestions.text = if (selectedQuestionType == null) {
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
        if (filteredQuestions.isEmpty()) return

        currentQuestionIndex = index
        val question = filteredQuestions[index]

        // 隐藏无题目提示
        view.noQuestionsView.visibility = View.GONE

        // 更新进度显示
        view.tvProgress.text = "${index + 1}/${filteredQuestions.size}"
        updatePageInfo()

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

        // 隐藏正确答案（如果需要显示，可以调用showCorrectAnswer()）
        view.tvCorrectAnswer.visibility = View.GONE

        // 更新按钮状态
        updateButtonStates()
    }

    private fun createSingleChoiceOptions(question: ErrorQuizItem) {
        val radioGroup = RadioGroup(this)
        val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
            .filter { it.isNotBlank() }

        options.forEachIndexed { index, optionText ->
            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                text = optionText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setLineSpacing(1.2f, 1.2f)

                // 设置选中监听
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val selectedOption = ('A'.toInt() + index).toChar().toString()
                        userAnswers[currentQuestionIndex] = selectedOption
                    }
                }

                // 恢复已选答案
                if (userAnswers[currentQuestionIndex] == ('A'.toInt() + index).toChar().toString()) {
                    isChecked = true
                }
            }
            radioGroup.addView(radioButton)
        }

        view.optionsContainer.addView(radioGroup)
    }

    private fun createMultipleChoiceOptions(question: ErrorQuizItem) {
        // 简化为示例，实际需要更复杂的多选逻辑
        question.options.forEachIndexed { index, optionText ->
            val checkBox = CheckBox(this).apply {
                id = View.generateViewId()
                text = optionText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setLineSpacing(1.2f, 1.2f)
            }
            view.optionsContainer.addView(checkBox)
        }
    }

    private fun createTrueFalseOptions(question: ErrorQuizItem) {
        val radioGroup = RadioGroup(this)
        val options = listOf("正确", "错误")

        options.forEachIndexed { index, optionText ->
            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                text = optionText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setLineSpacing(1.2f, 1.2f)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        userAnswers[currentQuestionIndex] = optionText
                    }
                }

                if (userAnswers[currentQuestionIndex] == optionText) {
                    isChecked = true
                }
            }
            radioGroup.addView(radioButton)
        }

        view.optionsContainer.addView(radioGroup)
    }

    private fun createFillBlankOptions(question: ErrorQuizItem) {
        val editText = EditText(this).apply {
            hint = "请输入答案"
            textSize = 16f
            setPadding(16, 16, 16, 16)

            // 设置文本变化监听
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    userAnswers[currentQuestionIndex] = editable.toString()
                }
            })

            // 恢复已填答案
            userAnswers[currentQuestionIndex]?.let {
                setText(it)
            }
        }

        view.optionsContainer.addView(editText)
    }

    private fun createUnknownTypeOptions(question: ErrorQuizItem) {
        val textView = TextView(this).apply {
            text = "未知题型，无法显示选项"
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        view.optionsContainer.addView(textView)
    }

    private fun updateButtonStates() {
        view.btnPrevious.isEnabled = currentQuestionIndex > 0 || currentPage > 1
        view.btnNext.isEnabled = currentQuestionIndex < filteredQuestions.size - 1 || currentPage < totalPages
    }

    private fun updatePageInfo() {
        // 这里可以根据需要添加页面信息显示
    }

    private fun showCorrectAnswer() {
        if (filteredQuestions.isEmpty()) return

        val question = filteredQuestions[currentQuestionIndex]
        view.tvCorrectAnswer.text = "正确答案: ${question.correctOption}"
        view.tvCorrectAnswer.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("QuizActivity", "Activity销毁")
    }
}