package com.jxdx.square.plaza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jxdx.square.R
import com.jxdx.square.adapter.TopAdapter

class TopFragment(private val callback: () -> Unit, ) : Fragment() {
    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager2? = null
    private var mTopAdapter: TopAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_top, container, false)
        initView(view)
        initViewPager()
        return view
    }

    private fun initView(view: View) {
        // 初始化TabLayout和ViewPager2
        mTabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        mViewPager = view.findViewById<ViewPager2>(R.id.sub_view_pager)
    }

    private fun initViewPager() {
        // 创建适配器
        mTopAdapter = TopAdapter(requireActivity(), callback)
        // 设置ViewPager2的适配器
        mViewPager!!.adapter = mTopAdapter

        // 连接TabLayout和ViewPager2
        TabLayoutMediator(
            mTabLayout!!,
            mViewPager!!,
        ) { tab, position ->
            // 从适配器获取标签文本并设置
            tab.setText(mTopAdapter!!.getTabTitle(position))
        }.attach()

        // 设置默认选中"动态"标签页（位置1）
        mViewPager!!.currentItem = 1
    }
}
