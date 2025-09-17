package com.jxdx.common.http.service

import androidx.fragment.app.Fragment

interface ResourceService {
    fun getFragment(fragmentName: String): Fragment?
}