package com.jxdx.home

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jxdx.common.http.service.FragmentService
import com.jxdx.common.http.service.ResourceService
import com.jxdx.common.http.service.ServiceRegistry
import com.jxdx.common.http.service.SquareService
import com.jxdx.home.databinding.ActivityHomeBinding

class TeacherHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var tvHome: TextView
    private lateinit var tvSquare: TextView
    private lateinit var tvClassTextView: TextView
    private lateinit var tvResource: TextView
    private lateinit var tvProfile: TextView

    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupClickListeners()

        // 默认显示首页
        switchFragment(currentTab)
    }

    private fun initViews() {
        // 获取文本视图
        tvHome = binding.navHome.getChildAt(1) as TextView
        tvSquare = binding.navSquare.getChildAt(1) as TextView
        tvClassTextView = binding.navClass.getChildAt(1) as TextView // 圆形按钮中的文本
        tvResource = binding.navResource.getChildAt(1) as TextView
        tvProfile = binding.navProfile.getChildAt(1) as TextView
    }

    private fun setupClickListeners() {
        binding.navHome.setOnClickListener { switchFragment(0) }
        binding.navSquare.setOnClickListener { switchFragment(1) }
        binding.navClass.setOnClickListener { switchFragment(2) }
        binding.navResource.setOnClickListener { switchFragment(3) }
        binding.navProfile.setOnClickListener { switchFragment(4) }
    }

    private fun switchFragment(position: Int) {
        currentTab = position
        resetAllTabs()
        val fragmentService = ServiceRegistry.get(FragmentService::class.java)
        val resourceService= ServiceRegistry.get(ResourceService::class.java)
        val squareService= ServiceRegistry.get(SquareService::class.java)
        if (fragmentService == null) {
            Log.d("--Home", "FragmentService未注册")
        }else{
            Log.d("---Home", "FragmentService已注册")
        }

        val fragment = when (position) {
            0 -> {
                binding.ivHome.setImageResource(R.drawable.ketang)
                tvHome.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                fragmentService?.getFragment("my")
            }

            1 -> {
                binding.ivSquare.setImageResource(R.drawable.ketang)
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
                binding.navClass.setBackgroundResource(R.drawable.bg_circle_primary_selected)
                binding.ivClass.setImageResource(R.drawable.ketang)
                tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                fragmentService?.getFragment("my")

            }

            3 -> {
                binding.ivResource.setImageResource(R.drawable.ketang)
                tvResource.setTextColor(ContextCompat.getColor(this, R.color.purple_200))
                resourceService?.getFragment("Resource")

            }

            4 -> {
                binding.ivProfile.setImageResource(R.drawable.ketang)
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
        binding.ivHome.setImageResource(R.drawable.ketang)
        binding.ivSquare.setImageResource(R.drawable.ketang)
        binding.ivResource.setImageResource(R.drawable.ketang)
        binding.ivProfile.setImageResource(R.drawable.ketang)

        tvHome.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvSquare.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvResource.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        tvProfile.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        // 重置圆形按钮
        binding.navClass.setBackgroundResource(R.drawable.bg_circle_primary)
        binding.ivClass.setImageResource(R.drawable.ketang)

        tvClassTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
}