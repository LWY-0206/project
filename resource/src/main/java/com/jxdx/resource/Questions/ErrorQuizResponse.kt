package com.jxdx.resource.Questions

data class ErrorQuizResponse(
    val code: Int,
    val message: String,
    val data: ErrorQuizData
)

data class ErrorQuizData(
    val records: List<ErrorQuizItem>,
    val total: Int,
    val size: Int,
    val current: Int,
    val pages: Int
)

data class ErrorQuizItem(
    val questionId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val options: List<String>,
    val correctOption: String,
    val userAnswer: String,
    val isMastered: Boolean,
    val createdTime: String,
    val correctResult: String,
    val trueFalseUserAnswer: String
) {
    // 判断题目类型
    fun getQuestionType(): QuestionType {
        return when {
            questionText.contains("【单选题】") -> QuestionType.SINGLE_CHOICE
            questionText.contains("【多选题】") -> QuestionType.MULTIPLE_CHOICE
            questionText.contains("【判断题】") -> QuestionType.TRUE_FALSE
            questionText.contains("【填空题】") -> QuestionType.FILL_BLANK
            else -> QuestionType.UNKNOWN
        }
    }
}

enum class QuestionType {
    SINGLE_CHOICE,  // 单选题
    MULTIPLE_CHOICE, // 多选题
    TRUE_FALSE,     // 判断题
    FILL_BLANK,     // 填空题
    UNKNOWN         // 未知类型
}