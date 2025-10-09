package com.jxdx.resource.Questions
import android.os.Parcel
import android.os.Parcelable

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
// 练习会话
data class PracticeSession(
    val sessionId: Int,
    val subjectId: Int,
    val currentBatch: Int,
    val totalBatches: Int,
    val completed: Boolean,
    val nextQuestion: Int,
    val remainingCount: Int,
    val pendingQueue: List<ErrorQuizItem>
)

// 提交答案响应
data class SubmitResponse(
    val sessionId: Int,
    val nextQuestion: Int,
    val remainingCount: Int,
    val batchCompleted: Boolean,
    val allCompleted: Boolean
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(questionId)
        parcel.writeString(questionText)
        parcel.writeString(optionA)
        parcel.writeString(optionB)
        parcel.writeString(optionC)
        parcel.writeString(optionD)
        parcel.writeStringList(options)
        parcel.writeString(correctOption)
        parcel.writeString(userAnswer)
        parcel.writeByte(if (isMastered) 1 else 0)
        parcel.writeString(createdTime)
        parcel.writeString(correctResult)
        parcel.writeString(trueFalseUserAnswer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ErrorQuizItem> {
        override fun createFromParcel(parcel: Parcel): ErrorQuizItem {
            return ErrorQuizItem(parcel)
        }

        override fun newArray(size: Int): Array<ErrorQuizItem?> {
            return arrayOfNulls(size)
        }
    }

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

// 在 ErrorQuizResponse.kt 中修改 QuestionType 枚举
enum class QuestionType(val value: Int) {
    COMPREHENSIVE(1),    // 综合题
    SINGLE_CHOICE(2),    // 单选题
    MULTIPLE_CHOICE(3),  // 多选题
    TRUE_FALSE(4),       // 判断题
    FILL_BLANK(5),       // 填空题
    UNKNOWN(0)           // 未知类型
}
