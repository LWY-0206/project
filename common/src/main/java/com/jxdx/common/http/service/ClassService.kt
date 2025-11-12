package com.jxdx.common.http.service

import android.content.Context
import androidx.fragment.app.Fragment

interface ClassService {
    fun getClassFragment(fragmentName: String): Fragment?
    fun navigateToGroupSeatActivity(context: Context)
    fun navigateToTeacherViewActivity(context: Context)
}