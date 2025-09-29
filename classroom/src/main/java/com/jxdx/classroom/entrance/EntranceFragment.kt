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
import com.jxdx.classroom.Subject
import com.jxdx.classroom.UserInfo
import com.jxdx.classroom.activity.ActivityToClassRoomFragment
import com.jxdx.classroom.activity.ActivityToTeacherClassRoomFragment
import com.jxdx.classroom.databinding.FragmentEntranceBinding
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.common.http.service.MineService
import com.jxdx.common.http.service.ServiceRegistry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.getValue
import androidx.fragment.app.viewModels

class EntranceFragment : Fragment() {

    private var _binding: FragmentEntranceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val viewModel: SubjectViewModel by viewModels()
    private var subjects: List<Subject> = listOf()
    // 添加用于自动滚动的Runnable变量
    private lateinit var autoScrollRunnable: Runnable
    private var identity = 0

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

        viewModel.getSubject()
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


        // 查询用户的所有课程
        viewModel.subjectsList.observe(requireActivity()){
            subjects = it?.mapIndexed { index, course ->
                val iconIndex = index % iconReS.size
                Subject(
                    course?.subjectName ?: "科目${index + 1}",
                    iconReS[iconIndex]
                )
            } ?: emptyList()


                var adapter = SubjectAdapter(
                    subjects,
                    onSubjectClick = {
                        ServiceRegistry.get(MineService::class.java)?.navigationToCourseActivity(requireContext())
                    }
                )
                viewPager.adapter = adapter
                viewPager.offscreenPageLimit = 3
                viewPager.setCurrentItem(1, false) // 初始定位到开始实现循环

                // 如果适配器已存在，通知数据更新
                adapter?.notifyDataSetChanged()

                // 如果有数据，启动自动滚动
                if (subjects.isNotEmpty()) {
                    startAutoScroll()
                }
                // 如果无数据，停止自动滚动
                else if (subjects.isEmpty()) {
                    stopAutoScroll()
                }

        }

        // 退出按钮
        binding.exit.setOnClickListener {
            Toast.makeText(requireContext(), "退出登录", Toast.LENGTH_SHORT).show()
        }

        //作业按钮
        binding.ivHomework.setOnClickListener {
            ServiceRegistry.get(MineService::class.java)?.navigationToHomeworkActivity(requireContext())
        }

        //班级按钮
        binding.ivClass.setOnClickListener {
            ServiceRegistry.get(MineService::class.java)?.navigationToGradeActivity(requireContext())
        }


        //直播按钮
        binding.ibLive.setOnClickListener {
            if(identity==0) {
                startActivity(Intent(requireContext(), ActivityToClassRoomFragment::class.java))//学生
            }else{
                startActivity(Intent(requireContext(), ActivityToTeacherClassRoomFragment::class.java))//老师
            }
        }
        updateEntranceUseInfo()
    }

    /**
     * 自动轮播逻辑
     */
    private fun startAutoScroll() {
        autoScrollRunnable = object : Runnable {
            override fun run() {
                currentPage++
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 2000) // 每2秒切换一次
            }
        }
        handler.postDelayed(autoScrollRunnable, 2000)
    }

    /**
     * 停止自动轮播
     */
    private fun stopAutoScroll() {
        if (::autoScrollRunnable.isInitialized) {
            handler.removeCallbacks(autoScrollRunnable)
        }
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
                            if(it.data?.identity ==0) {
                                identity=0
                                binding.tvUserName.text = "欢迎" + it.data?.userName + "同学！"
                                Glide.with(requireContext())
                                    .load(it.data?.avatarUrl)
                                    .circleCrop()
                                    .into(binding.ivAvatar)
                            }else{
                                identity=1
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
