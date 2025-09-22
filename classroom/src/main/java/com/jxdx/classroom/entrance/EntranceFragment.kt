package com.jxdx.classroom.entrance

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.jxdx.classroom.R
import com.jxdx.classroom.activity.ActivityToClassRoomFragment
import com.jxdx.classroom.databinding.FragmentEntranceBinding

class EntranceFragment : Fragment() {

    private var _binding: FragmentEntranceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 使用 ActivityEntranceBinding 作为布局文件
        _binding = FragmentEntranceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewpagerSubjects

        // 学科数据源
        val subjects = listOf(
            Subject("语文", R.drawable.ic_chinese),
            Subject("数学", R.drawable.ic_math),
            Subject("英语", R.drawable.ic_english),
            Subject("物理", R.drawable.ic_physics),
            Subject("化学", R.drawable.ic_chemistry),
            Subject("生物", R.drawable.ic_biology),
            Subject("历史", R.drawable.ic_history),
            Subject("政治", R.drawable.ic_politics)
        )

        // 设置 ViewPager2
        viewPager.adapter = SubjectAdapter(subjects)
        viewPager.offscreenPageLimit = 3
        viewPager.setCurrentItem(Int.MAX_VALUE / 2, false) // 初始定位到中间实现循环

        // 启动自动滚动
        startAutoScroll()

        // 退出按钮
        binding.exit.setOnClickListener {
            Toast.makeText(requireContext(), "退出登录", Toast.LENGTH_SHORT).show()
        }


        binding.ibLive.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityToClassRoomFragment::class.java))
        }
    }

    /**
     * 自动轮播逻辑
     */
    private fun startAutoScroll() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentPage++
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 2000) // 每2秒切换一次
            }
        }, 2000)
    }

    /**
     * 生命周期安全处理：Fragment 销毁视图时移除 Handler 回调
     */
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
