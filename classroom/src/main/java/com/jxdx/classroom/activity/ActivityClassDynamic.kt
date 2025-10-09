package com.jxdx.classroom.activity

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityClassdynamicBinding
import com.jxdx.classroom.entity.Classroom
import com.jxdx.classroom.group.TeacherViewActivity

class ActivityClassDynamic: BaseActivity<ActivityClassdynamicBinding>() {

    private lateinit var classAdapter: ClassAdapter
    private lateinit var viewModel: ClassViewModel
    private lateinit var createLiveRoomViewModel: CreateLiveRoomViewModel
    private var subjectId: Int = 0
    private var subjectName: String = ""
    private var teacherId: Int = 0 // 添加teacherId成员变量
    
    override fun bindLayout(): ActivityClassdynamicBinding {
        return ActivityClassdynamicBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 接收Intent传递的数据
        intent?.let {
            subjectId = it.getIntExtra("subjectId", 0)
            subjectName = it.getStringExtra("subjectName") ?: ""
            teacherId = it.getIntExtra("teacherId", 0)
            // 添加日志，帮助调试参数传递问题
            Log.d("ClassDynamic", "接收到的参数: subjectId=$subjectId, subjectName=$subjectName, teacherId=$teacherId")
        }
        
        // 更新顶部标题文本框
        if (subjectName.isNotEmpty()) {
            view.tvTitle.text = subjectName
        }
        
        // 返回按钮点击事件
        view.btnBack.setOnClickListener {
            finish()
        }
        
        // 开始直播按钮点击事件
        view.btnStartLive.setOnClickListener {
            val selectedClassIds = classAdapter.getSelectedClassIds()
            if (selectedClassIds.isEmpty()) {
                Toast.makeText(this, "请至少选择一个班级", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 创建直播房间
            createLiveRoomViewModel.createLiveRoom(
                subjectId = subjectId,
                roomName = subjectName,
                classIds = selectedClassIds
            )
        }
        
        // 课件预览按钮点击事件
        view.btnPdfPreview.setOnClickListener {
            val intent = Intent(this, PdfPreviewActivity::class.java)
            startActivity(intent)
        }
        
        // 学生分组按钮点击事件
        view.btnStudentGroup.setOnClickListener {
            // 使用成员变量teacherId，而不是再次从intent中获取
            val jumpIntent = Intent(this, TeacherViewActivity::class.java)
            jumpIntent.putExtra("teacherId", teacherId)
            jumpIntent.putExtra("subjectId", subjectId)
            // 添加日志，记录传递给TeacherViewActivity的参数
            Log.d("ClassDynamic", "跳转到TeacherViewActivity: teacherId=$teacherId, subjectId=$subjectId")
            startActivity(jumpIntent)
        }
        
        // 习题准备按钮点击事件
        view.btnExercisePreparation.setOnClickListener {
            // 这里可以实现跳转到习题准备相关的页面
            // 目前先显示一个提示信息
            val intent= Intent(this, ActivityUpdateQuestion::class.java)
            startActivity( intent)
            
            // 日志记录
            Log.d("ClassDynamic", "点击了习题准备按钮，teacherId=$teacherId, subjectId=$subjectId")
        }
        
        // 预习查看按钮点击事件
        view.btnPreviewCheck.setOnClickListener {
            // 这里可以实现跳转到预习查看相关的页面
            // 目前先显示一个提示信息
            Toast.makeText(this, "预习查看功能开发中", Toast.LENGTH_SHORT).show()
            
            // 日志记录
            Log.d("ClassDynamic", "点击了预习查看按钮，teacherId=$teacherId, subjectId=$subjectId")
        }
        
        // 初始化RecyclerView和适配器
        initRecyclerView()
        
        // 初始化ViewModel
        initViewModel()
    }

    private fun initRecyclerView() {
        // 创建适配器
        classAdapter = ClassAdapter()
        
        // 设置选择状态变化监听
        classAdapter.onSelectionChanged = { selectedIds ->
            // 更新按钮状态或显示选中数量
            val count = selectedIds.size
            view.btnStartLive.text = if (count > 0) "开始直播 ($count)" else "开始直播"
        }
        
        // 设置LayoutManager
        view.rvClassInfo.layoutManager = LinearLayoutManager(this)
        
        // 设置适配器
        view.rvClassInfo.adapter = classAdapter
    }
    
    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ClassViewModel::class.java]
        createLiveRoomViewModel = ViewModelProvider(this)[CreateLiveRoomViewModel::class.java]
    }

    override fun subscribeUi() {
        // 观察网络请求结果
        viewModel.classroomData.observe(this) { result ->
            result.onSuccess { data ->
                if (data != null) {
                    Log.d("ActivityClassDynamic", "获取到班级数据: ${data.size}条")
                    classAdapter.clearAndAdd(data)
                } else {
                    Log.w("ActivityClassDynamic", "班级数据为空")
                }
            }
            result.onError { error, _ ->
                Log.e("ActivityClassDynamic", "获取班级数据失败：$error")
            }
        }
        
        // 观察创建直播房间的结果
        createLiveRoomViewModel.createLiveRoomData.observe(this) { result ->
            result.onSuccess { liveId ->
                Toast.makeText(this, "直播房间创建成功", Toast.LENGTH_SHORT).show()
                // 获取返回的liveId，用于后续跳转
                if (liveId != null) {
                    Log.d("ActivityClassDynamic", "创建直播房间成功，liveId: $liveId")
                    // 跳转到直播页面，并传递liveId和subjectName
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("liveId", liveId)
                    intent.putExtra("subjectName", subjectName)
                    startActivity(intent)
                }
            }
            result.onError { error, _ ->
                Log.e("ActivityClassDynamic", "创建直播房间失败，详细错误：$error")
                Log.e("ActivityClassDynamic", "错误类型：${error?.javaClass?.simpleName}")
                Log.e("ActivityClassDynamic", "错误消息：${error?.message}")
                
                // 根据错误类型显示不同的提示
                val errorMessage = when {
                    error?.message?.contains("网络") == true -> "网络连接异常，请检查网络设置"
                    error?.message?.contains("超时") == true -> "请求超时，请稍后重试"
                    error?.message?.contains("404") == true -> "接口不存在，请联系开发人员"
                    error?.message?.contains("500") == true -> "服务器内部错误，请稍后重试"
                    error?.message?.contains("401") == true -> "认证失败，请重新登录"
                    error?.message?.contains("403") == true -> "权限不足，无法执行此操作"
                    else -> "创建直播房间失败：${error?.message ?: "未知错误"}"
                }
                
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // 发起网络请求
        viewModel.getClassRoom(subjectId)
    }
}