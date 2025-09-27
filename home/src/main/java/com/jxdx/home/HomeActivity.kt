package com.jxdx.home

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jxdx.common.http.service.ClassService
import com.jxdx.common.http.service.MineService
import com.jxdx.common.http.service.ResourceService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.common.http.service.SquareService


class HomeActivity : AppCompatActivity() {

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
        val fragmentService = ServiceRegistry.get(MineService::class.java)
        val resourceService= ServiceRegistry.get(ResourceService::class.java)
        val squareService= ServiceRegistry.get(SquareService::class.java)
        var ClassService=ServiceRegistry.get(ClassService::class.java)
        var ResourceService=ServiceRegistry.get(ResourceService::class.java)
        if (fragmentService == null) {
            Log.d("--Home", "FragmentService未注册")
        }else{
            Log.d("---Home", "FragmentService已注册")
        }

        val fragment = when (position) {
            0 -> {
                ivHome.setImageResource(R.drawable.firstpage)
                tvHome.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                ResourceService?.getFragment("FirstFragment")
            }

            1 -> {
                ivSquare.setImageResource(R.drawable.ic_square)
                tvSquare.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                squareService?.getSquareTopFragment {
                    squareService.navigateToSendFragment(
                        this,
                        R.id.fragment_container // 传入容器ID
                    )
                }
            }

            2 -> {
                // 设置圆形按钮的选中状态
                navClass.setBackgroundResource(R.drawable.bg_circle_primary_selected)
                ivClass.setImageResource(R.drawable.ic_class)
                tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                ClassService?.getClassFragment("entrance")

            }

            3 -> {
                ivResource.setImageResource(R.drawable.ic_resource)
                tvResource.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                resourceService?.getFragment("Resource")

            }

            4 -> {
                ivProfile.setImageResource(R.drawable.ic_mine)
                tvProfile.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                fragmentService?.getFragment("my")

            }

            else ->fragmentService?.getFragment("my")

        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
        }
            ?.commit()

    }

    private fun resetAllTabs() {
        // 重置所有图标和文字颜色
        ivHome.setImageResource(R.drawable.firstpage1)
        ivSquare.setImageResource(R.drawable.ic_square1)
        ivResource.setImageResource(R.drawable.ic_resource1)
        ivProfile.setImageResource(R.drawable.ic_mine1)

        tvHome.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvSquare.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvResource.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvProfile.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        // 重置圆形按钮
        navClass.setBackgroundResource(R.drawable.bg_circle_primary)
        ivClass.setImageResource(R.drawable.ic_class1)

        tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
}