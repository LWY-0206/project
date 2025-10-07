package com.jxdx.classroom.entrance

import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.jxdx.common.http.service.ClassService
import com.jxdx.common.http.service.LoginService

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

        // 先更新用户信息，获取用户身份
        updateEntranceUseInfo()

        // 查询用户的所有课程
        viewModel.subjectsList.observe(requireActivity()){
            subjects = it?.mapIndexed { index, item ->
                val iconIndex = index % iconReS.size
                val subjectName = when (item) {
                    // 兼容AllCourse类型
                    is com.jxdx.classroom.AllCourse -> item.subjectName
                    // 兼容SubjectsVO类型
                    is com.jxdx.classroom.SubjectsVO -> item.subjectName
                    else -> "科目${index + 1}"
                }
                Subject(
                    subjectName,
                    iconReS[iconIndex]
                )
            } ?: emptyList()


                var adapter = SubjectAdapter(
                    subjects,
                    onSubjectClick = {
                        ServiceRegistry.get(MineService::class.java)?.navigationToCourseActivity(requireContext(),identity)
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
            // 清除登录信息
            val preferences: SharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            // 只清除登录相关的状态，保留用户数据
            editor.remove("is_logged_in")
            editor.remove("username")
            editor.remove("phone")
            editor.apply()
            
            ServiceRegistry.get(LoginService::class.java)?.navigateToLogin(requireContext())
        }

        //作业按钮
        binding.ivHomework.setOnClickListener {
            if(identity==1) {
                ServiceRegistry.get(MineService::class.java)?.navigationToCourseListActivity(requireContext())
            }else{
                ServiceRegistry.get(MineService::class.java)?.navigationToHomeworkActivity(requireContext())
            }
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
                val intent = Intent(requireContext(), ActivityToTeacherClassRoomFragment::class.java)
                // 获取用户信息并设置老师id参数
                RetrofitClient.apiService.getUserInfo().enqueue(object : Callback<BaseResp<UserInfo>> {
                    override fun onResponse(
                        call: Call<BaseResp<UserInfo>?>,
                        response: Response<BaseResp<UserInfo>?>?
                    ) {
                        if (response != null && response.isSuccessful) {
                            response.body()?.let { body ->
                                if (body.code == 0) {
                                    body.data?.let { userInfo ->
                                        // 从userInfo中获取userId字段作为teacherId
                                        val teacherId = userInfo.id
                                        Log.d("EntranceFragment",userInfo.toString())
                                        intent.putExtra("teacherId", teacherId)
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<BaseResp<UserInfo>?>,
                        t: Throwable
                    ) {
                        // 获取用户信息失败时，直接跳转
                        startActivity(intent)
                    }
                })
            }
        }
        binding.ivGroup.setOnClickListener {
            if(identity==0) {
                //学生
                ServiceRegistry.get(ClassService::class.java)?.navigateToGroupSeatActivity(requireContext())
            }else{
                //老师
                ServiceRegistry.get(ClassService::class.java)?.navigateToTeacherViewActivity(requireContext())
            }
        }
    }

    /**
     * 自动轮播逻辑
     */
    private fun startAutoScroll() {
        autoScrollRunnable = object : Runnable {
            override fun run() {
                currentPage++
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 5000) // 每5秒切换一次
            }
        }
        handler.postDelayed(autoScrollRunnable, 5000)
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
                response: Response<BaseResp<UserInfo>?>?
            ) {
                // 检查Fragment是否仍然附加到Activity并且视图有效
                if (!isAdded || _binding == null) {
                    return
                }
                
                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 0) {
                            if(it.data?.identity == 0) {
                                identity = 0
                                binding.tvUserName.text = "欢迎" + it.data?.userName + "同学！"
                                Log.d("EntranceFrance",it.data.toString())
                                Glide.with(requireContext())
                                    .load(it.data?.avatarUrl)
                                    .circleCrop()
                                    .into(binding.ivAvatar)
                            } else {
                                identity = 1
                                binding.tvUserName.text = "欢迎" + it.data?.userName + "！"
                                Log.d("EntranceFrance",it.data.toString())
                                Glide.with(requireContext())
                                    .load(it.data?.avatarUrl)
                                    .circleCrop()
                                    .into(binding.ivAvatar)
                            }
                            // 获取用户身份后，根据身份调用对应的接口
                            viewModel.getSubject(identity)
                        } else {
                            Toast.makeText(requireContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<BaseResp<UserInfo>?>,
                t: Throwable
            ) {
                // 检查Fragment是否仍然附加到Activity
                if (isAdded) {
                    Toast.makeText(requireContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
