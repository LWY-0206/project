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
import com.bumptech.glide.Glide
import com.example.corekit.http.bean.BaseResp
import com.jxdx.classroom.R
import com.jxdx.classroom.activity.ActivityToClassRoomFragment
import com.jxdx.classroom.activity.ActivityToTeacherClassRoomFragment
import com.jxdx.classroom.com.jxdx.classroom.fragment.TeacherClassRoomFragment
import com.jxdx.classroom.databinding.FragmentEntranceBinding
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.classroom.http.UserInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class EntranceFragment : Fragment() {

    private var _binding: FragmentEntranceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private var identity=0

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



        val iconReS=listOf(
            R.drawable.ig1,
            R.drawable.ic_math,
            R.drawable.ic_english,
            R.drawable.ic_physics,
            R.drawable.ic_chemistry,
            R.drawable.ic_biology,
            R.drawable.ic_history,
            R.drawable.ic_politics
        )
        var pos=0
        // 查询用户的所有课程
        val subjects = listOf(
            Subject("科目一", iconReS[pos++]),
            Subject("科目二", iconReS[pos++]),
            Subject("科目三", iconReS[pos++]),
            Subject("科目四", iconReS[pos++]),
            Subject("科目五", iconReS[pos++]),
            Subject("科目六", iconReS[pos++]),
            Subject("科目七", iconReS[pos++]),
            Subject("科目八", iconReS[pos]),
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


        //直播按钮
        binding.ibLive.setOnClickListener {
            if(identity==0) {
                startActivity(Intent(requireContext(), ActivityToClassRoomFragment::class.java))
            }else{
                startActivity(Intent(requireContext(), ActivityToTeacherClassRoomFragment::class.java))
            }
        }
        updateEntranceUseInfo()
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


    private fun updateEntranceUseInfo() {
        RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
            override fun onResponse(
                call: Call<BaseResp<UserInfo>?>,
                response: Response<BaseResp<UserInfo>?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code==0) {
                            identity= it.data?.identity ?:0
                            if(it.data?.identity ==0) {
                                binding.tvUserName.text = "欢迎" + it.data?.userName + "同学！"
                                Glide.with(requireContext())
                                    .load(it.data?.avatarUrl)
                                    .circleCrop()
                                    .into(binding.ivAvatar)
                            }else{
                                binding.tvUserName.text = "欢迎" + it.data?.userName + "老师！"
                                Glide.with(requireContext())
                                    .load(it.data?.avatarUrl)
                                    .circleCrop()
                                    .into(binding.ivAvatar)
                            }
                        }else{
                            Toast.makeText(requireContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<BaseResp<UserInfo>?>,
                t: Throwable
            ) {
                Toast.makeText(requireContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
