package com.jxdx.common.http.service

import android.content.Context
import androidx.fragment.app.Fragment

interface ResourceService {
    fun getFragment(fragmentName: String): Fragment?
    fun navigationTOQuizActivity(context: Context)
}