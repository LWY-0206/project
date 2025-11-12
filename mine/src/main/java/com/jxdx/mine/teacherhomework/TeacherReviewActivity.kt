package com.jxdx.mine.teacherhomework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.corekit.common.BaseActivity
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ActivityTeacherReviewBinding
import com.jxdx.mine.teacherhomework.fragment.UnreviewedHomeworkFragment
import com.jxdx.mine.teacherhomework.fragment.ReviewedHomeworkFragment

class TeacherReviewActivity : BaseActivity<ActivityTeacherReviewBinding>() {
    private var currentTab = 0 // 0: 未批改, 1: 已批改

    override fun bindLayout(): ActivityTeacherReviewBinding {
        Log.d("TeacherReviewActivity", "bindLayout 开始")
        try {
            val binding = ActivityTeacherReviewBinding.inflate(layoutInflater)
            Log.d("TeacherReviewActivity", "bindLayout 成功")
            return binding
        } catch (e: Exception) {
            Log.e("TeacherReviewActivity", "bindLayout 失败", e)
            throw e
        }
    }

    override fun initView() {
        Log.d("TeacherReviewActivity", "========== initView 开始 ==========")
        try {
            // 设置标题
            supportActionBar?.title = "作业批改"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            Log.d("TeacherReviewActivity", "ActionBar设置完成")
            
            // 初始化ViewPager
            initViewPager()
            
            Log.d("TeacherReviewActivity", "========== initView 完成 ==========")
        } catch (e: Exception) {
            Log.e("TeacherReviewActivity", "initView失败", e)
            e.printStackTrace()
            finish()
        }
    }

    override fun subscribeUi() {
        Log.d("TeacherReviewActivity", "========== subscribeUi 开始 ==========")
        try {
            // 设置返回按钮点击事件
            view.btnBack.setOnClickListener {
                Log.d("TeacherReviewActivity", "返回按钮点击")
                finish()
            }
            
            // 设置Tab点击事件
            view.tabUnreviewed.setOnClickListener {
                Log.d("TeacherReviewActivity", "未批改Tab点击")
                switchToTab(0)
            }
            
            view.tabReviewed.setOnClickListener {
                Log.d("TeacherReviewActivity", "已批改Tab点击")
                switchToTab(1)
            }
            
            Log.d("TeacherReviewActivity", "========== subscribeUi 完成 ==========")
        } catch (e: Exception) {
            Log.e("TeacherReviewActivity", "subscribeUi失败", e)
            e.printStackTrace()
        }
    }

    private fun initViewPager() {
        Log.d("TeacherReviewActivity", "========== initViewPager 开始 ==========")
        try {
            // 创建Fragment适配器
            val adapter = HomeworkPagerAdapter(this)
            view.viewPager.adapter = adapter
            
            // 设置ViewPager页面变化监听
            view.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.d("TeacherReviewActivity", "ViewPager页面切换到: $position")
                    updateTabSelection(position)
                }
            })
            
            Log.d("TeacherReviewActivity", "========== initViewPager 完成 ==========")
        } catch (e: Exception) {
            Log.e("TeacherReviewActivity", "initViewPager失败", e)
            e.printStackTrace()
        }
    }

    private fun switchToTab(tabIndex: Int) {
        Log.d("TeacherReviewActivity", "切换到Tab: $tabIndex")
        currentTab = tabIndex
        view.viewPager.currentItem = tabIndex
        updateTabSelection(tabIndex)
    }

    private fun updateTabSelection(tabIndex: Int) {
        Log.d("TeacherReviewActivity", "更新Tab选择状态: $tabIndex")
        currentTab = tabIndex
        
        if (tabIndex == 0) {
            // 未批改Tab选中
            view.tabUnreviewed.setTextColor(resources.getColor(android.R.color.white))
            view.tabUnreviewed.setBackgroundResource(R.drawable.bg_tab_selected)
            view.tabReviewed.setTextColor(resources.getColor(R.color.text_secondary))
            view.tabReviewed.setBackgroundResource(R.drawable.bg_tab_unselected)
        } else {
            // 已批改Tab选中
            view.tabReviewed.setTextColor(resources.getColor(android.R.color.white))
            view.tabReviewed.setBackgroundResource(R.drawable.bg_tab_selected)
            view.tabUnreviewed.setTextColor(resources.getColor(R.color.text_secondary))
            view.tabUnreviewed.setBackgroundResource(R.drawable.bg_tab_unselected)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("TeacherReviewActivity", "ActionBar返回按钮点击")
        finish()
        return true
    }

    // ViewPager适配器
    private class HomeworkPagerAdapter(activity: TeacherReviewActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UnreviewedHomeworkFragment()
                1 -> ReviewedHomeworkFragment()
                else -> UnreviewedHomeworkFragment()
            }
        }
    }
}
