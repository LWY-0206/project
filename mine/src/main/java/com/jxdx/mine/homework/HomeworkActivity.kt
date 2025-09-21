package com.jxdx.mine.homework

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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


/**
 * 作业数据模型
 */
data class Homework(
    val id: String,          // 作业ID
    val title: String,       // 作业标题
    val deadline: String,    // 截止日期
    val subject: String,     // 所属科目
    var status: Int          // 当前状态0未提交/1已提交未批改/3已完成
)
data class SubjectGroup(
    val subjectName: String,             // 科目名，如 数学、语文
    var isExpanded: Boolean = false,     // 是否展开
    val homeworkList: MutableList<Homework> // 科目下的作业列表
){
    // 添加一个唯一标识符用于比较
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SubjectGroup
        return subjectName == other.subjectName
    }

    override fun hashCode(): Int {
        return subjectName.hashCode()
    }
}