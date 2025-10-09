// QuizActivity.kt
package com.jxdx.resource.Questions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.jxdx.resource.databinding.ActivityQuizBinding

class QuizActivity : BaseActivity<ActivityQuizBinding>() {

    private lateinit var viewModel: ErrorQuizViewModel
    private var currentQuestionIndex = 0
    private var currentQuestions: MutableList<ErrorQuizItem> = mutableListOf()
    private var filteredQuestions: List<ErrorQuizItem> = emptyList()
    private var currentPage = 1
    private var totalPages = 1
    private val pageSize = 10
    private var subjectId = 1
    private var selectedQuestionType: QuestionType? = null
    private val userAnswers = mutableMapOf<Int, String>()
    private val answerResults = mutableListOf<Boolean>()
    private var isLoadingMore = false
    private var hasMoreQuestions = true

    // 练习会话相关变量
    private var isPracticeMode = true // 现在所有题型都使用练习模式
    private var currentSessionId: Int? = null // 当前会话ID
    private var remainingQuestions = mutableListOf<Int>() // 剩余题目ID列表
    private var isSubmitting = false // 是否正在提交答案
    private var isAnswerSubmitted = false // 当前题目是否已提交
    private var currentBatch = 1 // 当前批次
    private var totalBatches = 0 // 总批次

    private companion object {
        const val REQUEST_ANSWER_SHEET = 1001
        const val MIN_QUESTIONS_REQUIRED = 10
        const val DEFAULT_QUESTION_COUNT = 10 // 默认题目数量
    }

    override fun bindLayout(): ActivityQuizBinding {
        return ActivityQuizBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun initView() {
        viewModel = ViewModelProvider(this)[ErrorQuizViewModel::class.java]

        val questionTypeName = intent.getStringExtra("QUESTION_TYPE")
        Log.d("QuizActivity", "QuestionType: $questionTypeName")
        selectedQuestionType = if (questionTypeName != null) {
            QuestionType.valueOf(questionTypeName)
        } else {
            QuestionType.COMPREHENSIVE // 默认为综合题
        }
        // 设置题型标题
        view.tvQuestionType.text = getQuestionTypeTitle(selectedQuestionType)

        // 隐藏加载更多按钮，在练习模式下不需要
        view.btnLoadMore.visibility = View.GONE

        setupButtonListeners()
// 获取传递的题型参数

        // 所有题型都使用练习模式
        startPracticeSession()
    }

    private fun startPracticeSession() {
        // 清空当前题目
        currentQuestions.clear()
        filteredQuestions = emptyList()
        userAnswers.clear()
        answerResults.clear()
        remainingQuestions.clear()
        isAnswerSubmitted = false
        currentSessionId = null

        // 显示加载状态
        view.progressBar.visibility = View.VISIBLE
        view.noQuestionsView.visibility = View.GONE

        // 开始或恢复练习
        resumePractice()
    }

    private fun resumePractice() {
        val questionTypeValue = selectedQuestionType?.value ?: QuestionType.COMPREHENSIVE.value
        Log.d("QuizActivity", "开始或恢复练习，题型: $selectedQuestionType, 题目数量: $DEFAULT_QUESTION_COUNT")
        viewModel.resumePractice(subjectId,DEFAULT_QUESTION_COUNT,questionTypeValue)
    }

    override fun subscribeUi() {
        // 观察练习会话（所有题型都使用练习模式）
        viewModel.practiceSessionLiveData.observe(this) { resource ->
            resource
                .onSuccess { data ->
                    view.progressBar.visibility = View.GONE
                    isSubmitting = false

                    data?.let { session ->
                        // 保存会话信息
                        currentSessionId = session.sessionId
                        currentBatch = session.currentBatch
                        totalBatches = session.totalBatches

                        // 处理待处理队列
                        if (session.pendingQueue.isNotEmpty()) {
                            // 清空当前题目并添加新题目
                            currentQuestions.clear()
                            currentQuestions.addAll(session.pendingQueue)
                            filteredQuestions = currentQuestions

                            // 重置提交状态
                            isAnswerSubmitted = false

                            // 显示第一题
                            displayQuestion(0)
                            initAnswerResults()
                            view.noQuestionsView.visibility = View.GONE
                            updatePageInfo()

                            // 启用选项
                            enableAnswerOptions(true)
                            Log.d("QuizActivity", "练习会话加载成功，题目数量: ${session.pendingQueue.size}")
                        } else {
                            showNoQuestionsMessage()
                        }
                    }
                }
                .onError { error, data ->
                    view.progressBar.visibility = View.GONE
                    isSubmitting = false
                    showNoQuestionsMessage()
                    Toast.makeText(this, "加载练习失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                    // 启用选项允许重试
                    enableAnswerOptions(true)
                }
        }

        // 观察提交结果
        viewModel.submitQuestionLiveData.observe(this) { resource ->
            resource
                .onSuccess { data ->
                    isSubmitting = false
                    view.progressBar.visibility = View.GONE
                    data?.let { response ->
                        // 处理提交结果
                        handleSubmitResponse(response)
                    }
                }
                .onError { error, data ->
                    isSubmitting = false
                    view.progressBar.visibility = View.GONE
                    Toast.makeText(this, "提交答案失败: ${error?.message}", Toast.LENGTH_SHORT).show()
                    // 即使提交失败，也允许继续答题
                    enableAnswerOptions(true)
                }
        }
    }

    private fun handleSubmitResponse(response: SubmitResponse) {
        Log.d("QuizActivity", "处理提交响应: $response")

        if (response.allCompleted) {
            // 全部完成，显示完成信息
            Toast.makeText(this, "恭喜！所有题目都已掌握！", Toast.LENGTH_LONG).show()
            // 可以在这里添加完成后的操作，比如返回选择页面
            startActivity(Intent(this, QuestionSelectionActivity::class.java))
            finish()
        } else if (response.batchCompleted) {
            // 批次完成，获取下一批题目
            Toast.makeText(this, "当前批次完成！", Toast.LENGTH_SHORT).show()
            resumePractice()
        } else if (response.nextQuestion > 0) {
            // 有下一题，显示下一题
            Log.d("QuizActivity", "准备显示下一题: ${response.nextQuestion}")
            // 在当前题目列表中查找下一题
            val nextQuestionIndex = currentQuestions.indexOfFirst { it.questionId == response.nextQuestion }
            if (nextQuestionIndex != -1) {
                view.root.postDelayed({
                    displayQuestion(nextQuestionIndex)
                }, 1500) // 延时1.5秒
            } else {
                // 如果下一题不在当前列表中，重新获取练习会话
                resumePractice()
            }
        } else {
            // 没有下一题，重新获取练习会话
            Log.d("QuizActivity", "没有下一题，重新获取练习会话")
            resumePractice()
        }
    }

    private fun initAnswerResults() {
        answerResults.clear()
        filteredQuestions.forEach { _ ->
            answerResults.add(false)
        }
    }

    private fun setupButtonListeners() {
        view.btnBack.setOnClickListener {
            onBackPressed()
        }

        view.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                displayQuestion(currentQuestionIndex - 1)
            }
        }

        view.btnNext.setOnClickListener {
            if (currentQuestionIndex < filteredQuestions.size - 1) {
                displayQuestion(currentQuestionIndex + 1)
            } else {
                // 获取更多题目
                resumePractice()
            }
        }

        view.btnSubmit.setOnClickListener {
            // 提交当前题目答案
            submitCurrentQuestion()
        }

        view.btnBackToSelection.setOnClickListener {
            startActivity(Intent(this, QuestionSelectionActivity::class.java))
            finish()
        }

        view.btnLoadMore.setOnClickListener {
            // 练习模式下不需要加载更多
            Toast.makeText(this, "练习模式下会自动加载题目", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitCurrentQuestion() {
        if (isSubmitting || isAnswerSubmitted) return

        val currentQuestion = filteredQuestions.getOrNull(currentQuestionIndex) ?: return
        val userAnswer = userAnswers[currentQuestionIndex] ?: ""

        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "请先选择答案", Toast.LENGTH_SHORT).show()
            return
        }

        // 判断答案是否正确
        val isCorrect = when (currentQuestion.getQuestionType()) {
            QuestionType.SINGLE_CHOICE, QuestionType.TRUE_FALSE -> {
                userAnswer == currentQuestion.correctOption
            }
            QuestionType.MULTIPLE_CHOICE -> {
                // 多选题需要特殊处理，比较答案集合
                val userAnswerSet = userAnswer.split(",").toSet()
                val correctAnswerSet = currentQuestion.correctOption.split(",").toSet()
                userAnswerSet == correctAnswerSet
            }
            QuestionType.FILL_BLANK -> {
                userAnswer.trim().equals(currentQuestion.correctOption.trim(), ignoreCase = true)
            }
            else -> false
        }

        // 更新答题结果
        answerResults[currentQuestionIndex] = isCorrect

        // 显示正确答案
        showCorrectAnswer()

        // 显示答题结果
        val resultMessage = if (isCorrect) "回答正确！" else "回答错误！"
        Toast.makeText(this, resultMessage, Toast.LENGTH_SHORT).show()

        // 禁用选项，防止重复提交
        enableAnswerOptions(false)
        isSubmitting = true
        isAnswerSubmitted = true
        view.progressBar.visibility = View.VISIBLE

        // 提交答案（使用 sessionId）
        currentSessionId?.let { sessionId ->
            viewModel.submitAnswer(sessionId, currentQuestion.questionId, isCorrect)
        } ?: run {
            Toast.makeText(this, "会话信息丢失，请重新开始练习", Toast.LENGTH_SHORT).show()
            enableAnswerOptions(true)
            isSubmitting = false
        }
    }

    private fun enableAnswerOptions(enabled: Boolean) {
        // 禁用或启用所有选项控件
        for (i in 0 until view.optionsContainer.childCount) {
            val child = view.optionsContainer.getChildAt(i)
            when (child) {
                is RadioGroup -> {
                    for (j in 0 until child.childCount) {
                        child.getChildAt(j).isEnabled = enabled
                    }
                }
                is CheckBox -> child.isEnabled = enabled
                is EditText -> child.isEnabled = enabled
            }
        }

        // 禁用或启用提交按钮
        view.btnSubmit.isEnabled = enabled && !isAnswerSubmitted
    }

    private fun submitAnswersAndNavigate() {
        var answeredCount = 0
        var correctCount = 0

        filteredQuestions.forEachIndexed { index, question ->
            val userAnswer = userAnswers[index] ?: ""
            val isCorrect = answerResults.getOrNull(index) ?: false

            if (userAnswer.isNotEmpty()) {
                answeredCount++
                if (isCorrect) correctCount++
            }
        }

        val intent = Intent(this, AnswerSheetActivity::class.java).apply {
            putParcelableArrayListExtra("questions", ArrayList(filteredQuestions))
            putExtra("user_answers", HashMap(userAnswers))
            putExtra("answer_results", answerResults.toBooleanArray())
            putExtra("current_index", currentQuestionIndex)
            putExtra("subjectId", subjectId)
        }
        startActivityForResult(intent, REQUEST_ANSWER_SHEET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ANSWER_SHEET) {
            if (resultCode == RESULT_OK) {
                val questionIndex = data?.getIntExtra("question_index", currentQuestionIndex) ?: currentQuestionIndex
                displayQuestion(questionIndex)
                showCorrectAnswer()
            } else if (resultCode == RESULT_CANCELED) {
                startActivity(Intent(this, QuestionSelectionActivity::class.java))
                finish()
            }
        }
    }

    private fun getQuestionTypeTitle(questionType: QuestionType?): String {
        return when (questionType) {
            QuestionType.COMPREHENSIVE -> "综合刷题"
            QuestionType.SINGLE_CHOICE -> "单选题练习"
            QuestionType.MULTIPLE_CHOICE -> "多选题练习"
            QuestionType.TRUE_FALSE -> "判断题练习"
            QuestionType.FILL_BLANK -> "填空题练习"
            else -> "全部练习"
        }
    }

    private fun showNoQuestionsMessage() {
        view.noQuestionsView.visibility = View.VISIBLE
        view.tvNoQuestions.text = if (selectedQuestionType == QuestionType.COMPREHENSIVE) {
            "暂无题目"
        } else {
            "暂无${getQuestionTypeName(selectedQuestionType)}题目"
        }
    }

    private fun getQuestionTypeName(type: QuestionType?): String {
        return when (type) {
            QuestionType.COMPREHENSIVE -> "综合题"
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

        view.noQuestionsView.visibility = View.GONE
        view.tvProgress.text = "${index + 1}/${filteredQuestions.size}"
        updatePageInfo()

        view.tvQuestionContent.text = question.questionText
        view.optionsContainer.removeAllViews()

        when (question.getQuestionType()) {
            QuestionType.SINGLE_CHOICE -> createSingleChoiceOptions(question)
            QuestionType.MULTIPLE_CHOICE -> createMultipleChoiceOptions(question)
            QuestionType.TRUE_FALSE -> createTrueFalseOptions(question)
            QuestionType.FILL_BLANK -> createFillBlankOptions(question)
            else -> createUnknownTypeOptions(question)
        }

        // 重置正确答案显示
        view.tvCorrectAnswer.visibility = View.GONE
        updateButtonStates()

        // 重置提交状态
        isAnswerSubmitted = false

        // 启用选项
        enableAnswerOptions(true)
    }

    // 以下选项创建方法保持不变
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

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val selectedOption = ('A'.toInt() + index).toChar().toString()
                        userAnswers[currentQuestionIndex] = selectedOption
                    }
                }

                if (userAnswers[currentQuestionIndex] == ('A'.toInt() + index).toChar().toString()) {
                    isChecked = true
                }
            }
            radioGroup.addView(radioButton)
        }

        view.optionsContainer.addView(radioGroup)
    }

    private fun createMultipleChoiceOptions(question: ErrorQuizItem) {
        question.options.forEachIndexed { index, optionText ->
            val checkBox = CheckBox(this).apply {
                id = View.generateViewId()
                text = optionText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setLineSpacing(1.2f, 1.2f)

                setOnCheckedChangeListener { _, isChecked ->
                    val selectedOptions = userAnswers[currentQuestionIndex]?.split(",")?.toMutableSet() ?: mutableSetOf()
                    val optionKey = ('A'.toInt() + index).toChar().toString()

                    if (isChecked) {
                        selectedOptions.add(optionKey)
                    } else {
                        selectedOptions.remove(optionKey)
                    }

                    userAnswers[currentQuestionIndex] = selectedOptions.joinToString(",")
                }

                userAnswers[currentQuestionIndex]?.split(",")?.forEach { selectedOption ->
                    if (selectedOption == ('A'.toInt() + index).toChar().toString()) {
                        isChecked = true
                    }
                }
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

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(editable: Editable?) {
                    userAnswers[currentQuestionIndex] = editable.toString()
                }
            })

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
        view.btnPrevious.isEnabled = currentQuestionIndex > 0
        view.btnNext.isEnabled = currentQuestionIndex < filteredQuestions.size - 1
        view.btnLoadMore.isEnabled = false
        view.btnLoadMore.visibility = View.GONE

        // 修改按钮文本和状态
        view.btnSubmit.text = "提交答案"
        view.btnSubmit.isEnabled = !isAnswerSubmitted
    }

    private fun updatePageInfo() {
        view.tvPageInfo.text = "${getQuestionTypeTitle(selectedQuestionType)} | 批次: $currentBatch/$totalBatches | 剩余: ${filteredQuestions.size - currentQuestionIndex - 1}题"
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