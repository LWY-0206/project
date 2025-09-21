package com.jxdx.common.http.service

import androidx.fragment.app.Fragment

interface ClassService {
    fun getClassFragment(fragmentName: String): Fragment?
}