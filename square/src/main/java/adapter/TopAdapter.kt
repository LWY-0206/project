package com.example.loding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.loding.plaza.ContactFragment
import com.example.loding.plaza.DynamicFragment
import com.example.loding.plaza.GameFragement
import com.example.loding.plaza.MessageFragment

class TopAdapter(
    fragmentActivity: FragmentActivity,
    private val callback: () -> Unit,
) : FragmentStateAdapter(fragmentActivity) {
    // 存储所有子页面Fragment的列表，用于ViewPager2的顶部跳转
    private val mFragmentList =
        listOf(
            GameFragement(), // 益智PK页面
            DynamicFragment(callback), // 动态页面
            MessageFragment(), // 消息页面
            ContactFragment(), // 联系人页面
        )

    // 存储所有标签文本的列表
    private val mTabTitles =
        listOf(
            "益智PK",
            "动态",
            "消息",
            "联系人",
        )

    override fun createFragment(position: Int): Fragment {
        // 根据位置返回对应的Fragment
        return mFragmentList[position]
    }

    override fun getItemCount(): Int {
        // 返回Fragment的数量
        return mFragmentList.size
    }

    /**
     * 获取指定位置的标签文本
     * @param position 位置索引
     * @return 标签文本
     */
    fun getTabTitle(position: Int): String =
        if (position in mTabTitles.indices) {
            mTabTitles[position]
        } else {
            ""
        }
}
