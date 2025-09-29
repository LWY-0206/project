package com.jxdx.resource.StudySuggestions

import com.jxdx.resource.FirstPage.RecommendationData

data class StudyData (
    val subjectAnalyses:List<SubjectAnalyses>,
    val overallSuggestion:String

)
data class SubjectAnalyses (
    val  subject:String,
    val averageScore:Double,
    val totalAssignments:Int,
    val suggestion:String,
    val recommendedHours:Int,
)