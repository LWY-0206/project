package com.jxdx.mine.service

import androidx.fragment.app.Fragment
import com.jxdx.common.http.service.FragmentService

class MineFragmentServiceImpl : FragmentService{
    override fun getFragment(fragmentName: String): Fragment? {
        return when(fragmentName){
            "my"-> My()
            else -> My()
        }
    }
}