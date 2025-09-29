package com.jxdx.mine.homework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jxdx.mine.Homework
import com.jxdx.mine.SubjectGroup
import com.jxdx.mine.R

class HomeworkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homework)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val fragments = listOf(
            createFragment(0),
            createFragment(1),
            createFragment(2)
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "未提交"
                1 -> "待批改"
                2 -> "已完成"
                else -> ""
            }
        }.attach()
    }

    private fun createFragment(status: Int): HomeworkListFragment {
        val fragment = HomeworkListFragment()
        fragment.arguments = bundleOf("status" to status)
        return fragment
    }
}


// 数据模型已移至 com.jxdx.mine.data.kt 中统一管理