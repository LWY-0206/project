package com.jxdx.common.navigation

import androidx.fragment.app.Fragment

interface FragmentNavigationService {
    /**
     * 获取指定类型的 Fragment
     * @param fragmentTag 用于标识要获取的 Fragment 类型
     */
    fun getFragment(fragmentTag: String): Fragment?

    /**
     * 获取所有可用的 Fragment 标签
     */
    fun getAvailableFragmentTags(): List<String>
}