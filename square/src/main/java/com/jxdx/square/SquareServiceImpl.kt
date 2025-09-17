package com.jxdx.square
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

import com.jxdx.common.http.service.SquareService
import com.jxdx.square.plaza.SendFragment
import com.jxdx.square.plaza.TopFragment

class SquareServiceImpl: SquareService {

    override fun getSquareTopFragment(onActionClick: () -> Unit): Fragment {
        return TopFragment(onActionClick)
    }

    override fun navigateToSendFragment(activity: FragmentActivity, containerId: Int) {
        val sendFragment = SendFragment.newInstance()
        // 设置监听器等...
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, sendFragment)
            .addToBackStack("send_fragment")
            .commit()
    }
}