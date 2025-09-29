package com.jxdx.common.http.service

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment


interface MineService {
    fun getFragment(fragmentName: String): Fragment?
    fun navigationToHomeworkActivity(context: Context)
    fun navigationToGradeActivity(context: Context)
    fun navigationToCourseActivity(context: Context)
}