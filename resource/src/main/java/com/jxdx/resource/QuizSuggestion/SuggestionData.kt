package com.jxdx.resource.QuizSuggestion

import android.os.Parcel
import android.os.Parcelable

data class SuggestionData(
    val subjectName: String,
    val questionType: String,
    val questionCount: String,
    val suggestions: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(subjectName)
        parcel.writeString(questionType)
        parcel.writeString(questionCount)
        parcel.writeStringList(suggestions)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SuggestionData> {
        override fun createFromParcel(parcel: Parcel): SuggestionData {
            return SuggestionData(parcel)
        }

        override fun newArray(size: Int): Array<SuggestionData?> {
            return arrayOfNulls(size)
        }
    }
}