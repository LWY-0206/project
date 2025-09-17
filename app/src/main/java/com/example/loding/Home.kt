package com.example.loding

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jxdx.square.plaza.SendFragment
import com.jxdx.square.plaza.TopFragment

class Home : AppCompatActivity(), SendFragment.OnPublishListener {
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navSquare: LinearLayout
    private lateinit var navClass: LinearLayout
    private lateinit var navResource: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var ivHome: ImageView
    private lateinit var ivSquare: ImageView
    private lateinit var ivClass: ImageView
    private lateinit var ivResource: ImageView
    private lateinit var ivProfile: ImageView

    private lateinit var tvHome: TextView
    private lateinit var tvSquare: TextView
    private lateinit var tvClassTextView: TextView
    private lateinit var tvResource: TextView
    private lateinit var tvProfile: TextView

    private var currentTab = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupClickListeners()

        // 默认显示首页
        switchFragment(currentTab)
    }

    private fun initViews() {
        fragmentContainer = findViewById(R.id.fragment_container)

        navHome = findViewById(R.id.nav_home)
        navSquare = findViewById(R.id.nav_square)
        navClass = findViewById(R.id.nav_class) // 圆形按钮
        navResource = findViewById(R.id.nav_resource)
        navProfile = findViewById(R.id.nav_profile)

        ivHome = findViewById(R.id.iv_home)
        ivSquare = findViewById(R.id.iv_square)
        ivClass = findViewById(R.id.iv_class)
        ivResource = findViewById(R.id.iv_resource)
        ivProfile = findViewById(R.id.iv_profile)

        // 获取文本视图
        tvHome = navHome.getChildAt(1) as TextView
        tvSquare = navSquare.getChildAt(1) as TextView
        tvClassTextView = navClass.getChildAt(1) as TextView // 圆形按钮中的文本
        tvResource = navResource.getChildAt(1) as TextView
        tvProfile = navProfile.getChildAt(1) as TextView
    }

    private fun setupClickListeners() {
        navHome.setOnClickListener { switchFragment(0) }
        navSquare.setOnClickListener { switchFragment(1) }
        navClass.setOnClickListener { switchFragment(2) }
        navResource.setOnClickListener { switchFragment(3) }
        navProfile.setOnClickListener { switchFragment(4) }
    }

    private fun switchFragment(position: Int) {
        currentTab = position
        resetAllTabs()

        val fragment: Fragment =
            when (position) {
                0 -> {
                    ivHome.setImageResource(R.drawable.ketang)
                    tvHome.setTextColor(ContextCompat.getColor(this, R.color.purple_200))

                    TopFragment {
                        // 跳转到发布动态页面
                        val sendFragment = SendFragment.Companion.newInstance()
                        // 设置发布监听器
                        sendFragment.setOnPublishListener(this)
                        val transaction = this.supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, sendFragment)
                        transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                        transaction.commit()
                    }
                }
                1 -> {
                    ivSquare.setImageResource(R.drawable.ketang)
                    tvSquare.setTextColor(ContextCompat.getColor(this, R.color.purple_200))

                    TopFragment {
                        // 跳转到发布动态页面
                        val sendFragment = SendFragment.Companion.newInstance()
                        // 设置发布监听器
                        sendFragment.setOnPublishListener(this)
                        val transaction = this.supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, sendFragment)
                        transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                        transaction.commit()
                    }
                }
                2 -> {
                    // 设置圆形按钮的选中状态
                    navClass.setBackgroundResource(R.drawable.bg_circle_primary_selected)
                    ivClass.setImageResource(R.drawable.ketang)
                    tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_200))

                    TopFragment {
                        // 跳转到发布动态页面
                        val sendFragment = SendFragment.Companion.newInstance()
                        // 设置发布监听器
                        sendFragment.setOnPublishListener(this)
                        val transaction = this.supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, sendFragment)
                        transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                        transaction.commit()
                    }
                }
                3 -> {
                    ivResource.setImageResource(R.drawable.ketang)
                    tvResource.setTextColor(ContextCompat.getColor(this, R.color.purple_200))

                    TopFragment {
                        // 跳转到发布动态页面
                        val sendFragment = SendFragment.Companion.newInstance()
                        // 设置发布监听器
                        sendFragment.setOnPublishListener(this)
                        val transaction = this.supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, sendFragment)
                        transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                        transaction.commit()
                    }
                }
                4 -> {
                    ivProfile.setImageResource(R.drawable.ketang)
                    tvProfile.setTextColor(ContextCompat.getColor(this, R.color.purple_200))

                    TopFragment {
                        // 跳转到发布动态页面
                        val sendFragment = SendFragment.Companion.newInstance()
                        // 设置发布监听器
                        sendFragment.setOnPublishListener(this)
                        val transaction = this.supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, sendFragment)
                        transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                        transaction.commit()
                    }
                }
                else -> TopFragment {
                    // 跳转到发布动态页面
                    val sendFragment = SendFragment.Companion.newInstance()
                    // 设置发布监听器
                    sendFragment.setOnPublishListener(this)
                    val transaction = this.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, sendFragment)
                    transaction.addToBackStack(null) // 添加到返回栈，以便可以返回到当前页面
                    transaction.commit()
                }
            }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


    private fun resetAllTabs() {
        // 重置所有图标和文字颜色
        ivHome.setImageResource(R.drawable.ketang)
        ivSquare.setImageResource(R.drawable.ketang)
        ivResource.setImageResource(R.drawable.ketang)
        ivProfile.setImageResource(R.drawable.ketang)

        tvHome.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvSquare.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvResource.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvProfile.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        // 重置圆形按钮
        navClass.setBackgroundResource(R.drawable.bg_circle_primary)
        ivClass.setImageResource(R.drawable.ketang)

        tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun onPublishSuccess() {
        refreshData()
    }

    /**
     * 刷新数据
     */
    private fun refreshData() {
//        // 重置分页状态
//        currentPage = 1
//        hasMoreData = true
//
//        // 重新加载数据
//        viewModel.getDynamics(null, pageSize)
    }
}