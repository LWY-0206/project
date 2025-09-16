package com.jxdx.common.http.service

import androidx.fragment.app.Fragment


interface FragmentService {
    fun getFragment(fragmentName: String): Fragment?
}