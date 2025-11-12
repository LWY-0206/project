package com.jxdx.mine.homework

import android.util.Log
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.corekit.common.BaseActivity
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityHomeworkBinding

class HomeworkActivity : BaseActivity<ActivityHomeworkBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeworkActivity", "onCreate: 作业管理页面创建")
    }

    override fun bindLayout(): ActivityHomeworkBinding {
        return ActivityHomeworkBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 确保状态栏与Toolbar颜色一致，避免视觉重合
        val primaryColor = resources.getColor(R.color.primary_color, null)
        window.statusBarColor = primaryColor
        
        val tabLayout = view.tabLayout
        val viewPager = view.viewPager
        Log.d("HomeworkActivity", "TabLayout和ViewPager2初始化完成")

        // 创建三个Fragment，分别对应三种作业状态
        Log.d("HomeworkActivity", "开始创建三个HomeworkListFragment")
        val fragments = listOf(
            createFragment(0),
            createFragment(1),
            createFragment(2)
        )
        Log.d("HomeworkActivity", "三个HomeworkListFragment创建完成")

        // 设置ViewPager适配器
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        Log.d("HomeworkActivity", "ViewPager适配器设置完成")

        // 设置TabLayout与ViewPager联动
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabText = when (position) {
                0 -> "未提交"
                1 -> "待批改"
                2 -> "已完成"
                else -> ""
            }
            tab.text = tabText
            Log.d("HomeworkActivity", "Tab初始化: 位置=$position, 文本=$tabText")
        }.attach()
        Log.d("HomeworkActivity", "TabLayout与ViewPager联动设置完成")
    }

    override fun subscribeUi() {
        // 设置ViewPager页面切换监听
        view.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val statusText = when (position) {
                    0 -> "未提交"
                    1 -> "待批改"
                    2 -> "已完成"
                    else -> "未知"
                }
                Log.d("HomeworkActivity", "页面切换: 位置=$position, 显示${statusText}作业")
            }
        })
    }

    private fun createFragment(status: Int): HomeworkListFragment {
        val fragment = HomeworkListFragment()
        fragment.arguments = bundleOf("status" to status)
        val statusText = when (status) {
            0 -> "未提交"
            1 -> "待批改"
            2 -> "已完成"
            else -> "未知"
        }
        Log.d("HomeworkActivity", "创建Fragment: 状态=$status($statusText)")
        return fragment
    }
}