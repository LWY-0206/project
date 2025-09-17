package com.jxdx.common.http.service

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface SquareService {
    fun getSquareTopFragment(onActionClick: () -> Unit): Fragment
    fun navigateToSendFragment(activity: FragmentActivity,containerId: Int)
}