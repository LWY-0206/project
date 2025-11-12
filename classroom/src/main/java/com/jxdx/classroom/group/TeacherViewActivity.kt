package com.jxdx.classroom.group

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.jxdx.classroom.R
import com.jxdx.classroom.databinding.ActivityTeacherViewBinding
import com.jxdx.classroom.http.RetrofitClient
import com.jxdx.classroom.http.DTO.GenerateGroupDTO
import com.jxdx.classroom.http.DTO.FreeDistribution
import com.example.corekit.http.TokenManager
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import java.util.*
import java.util.concurrent.TimeUnit

class TeacherViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherViewBinding
    private val groups = mutableListOf<Group>()
    private var subjectId = 1 // 默认值，将在onCreate中更新
    private var groupNumber = 0 // 默认值，将在onCreate中更新
    private var teacherId = 6 // 默认值，将在onCreate中更新
    
    // 创建与Activity生命周期绑定的协程作用域
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // WebSocket相关
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private val wsBaseUrl = "ws://121.41.176.238:8080/group/chat/"
    private var isConnected = false
    private var webSocket: WebSocket? = null
    // 存储初始连接时的Token，用于重连时使用
    private var initialToken: String? = null
    
    // 请求码
    companion object {
        private const val REQUEST_SELECT_QUESTION_TYPE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取从上一个界面传递的subjectId和teacherId
        subjectId = intent.getIntExtra("subjectId", 1)
        teacherId = intent.getIntExtra("teacherId", 6)
        
        // 添加日志，记录接收到的参数
        Log.d("TeacherViewActivity", "onCreate接收到的参数: subjectId=$subjectId, teacherId=$teacherId")
        // 初始化视图
        initView()
        
        // 加载分组数据
        loadGroupsData()
        setOnClickListener()
        // WebSocket连接将在小组数据加载完成后自动建立

    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消所有协程，防止内存泄漏
        activityScope.cancel()
        // 断开WebSocket连接
        disconnectWebSocket()
    }

    override fun onResume() {
        super.onResume()
        // 在返回到该页面时重新加载小组数据，确保删除全部小组后能正确刷新
        loadGroupsData()
    }
    
    // 删除旧的connectWebSocket方法，新的实现在后面

    //初始化试图
    private fun initView(){
        binding.tvTitle.text = "小组监控面板"
        binding.tvTotalGroups.text=groups.size.toString()
        
        // 为广播按钮添加长按事件，用于导航到题型列表
        binding.tvBroad.setOnLongClickListener {
            navigateToQuestionTypeList()
            true
        }
    }
    
    /**
     * 导航到题型列表页面
     */
    private fun navigateToQuestionTypeList() {
        // 启动题型列表Activity（管理模式）
        val intent = Intent(this, QuestionTypeListActivity::class.java)
        intent.putExtra("selectMode", false)
        startActivity(intent)
    }

    //点击事件
    private fun setOnClickListener(){
        binding.ivBack.setOnClickListener { finish() }
        //分组数量按钮点击事件
        binding.tvNumber.setOnClickListener {
            showSetGroupSizeDialog()
        }
        //广播消息按钮
        binding.tvBroad.setOnClickListener {
            showBroadcastOptions()
        }
        
        // 随机分组按钮点击事件
        binding.btnRandom.setOnClickListener {
            // 显示确认对话框
            val builder = AlertDialog.Builder(this)
            builder.setTitle("随机分组确认")
            builder.setMessage("确定要将未入组的学生随机分配到各个小组吗？")
            
            builder.setPositiveButton("确定") { dialog, which ->
                // 执行自由分配未入组成员操作
                performFreeDistribution()
            }
            
            builder.setNegativeButton("取消", null)
            
            builder.show()
        }
    }
    
    /**
     * 执行自由分配未入组成员操作
     */
    private fun performFreeDistribution() {
        val token = TokenManager.getToken() ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Token获取失败，无法执行分配操作", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示加载提示
        Toast.makeText(this, "正在执行随机分配...", Toast.LENGTH_SHORT).show()
        
        activityScope.launch {
            try {
                // 创建FreeDistribution请求体
                val requestBody = FreeDistribution(
                    createdBy = teacherId,
                    subjectId = subjectId
                )
                
                // 调用自由分配未入组成员接口
                val response = RetrofitClient.apiService.freeDistribution(
                    satoken = token,
                    request = requestBody
                )
                
                // 检查响应是否成功
                if (response.code == 0) {
                    Toast.makeText(this@TeacherViewActivity, "随机分配成功", Toast.LENGTH_SHORT).show()
                    
                    // 随机分配成功后不立即重新加载数据，避免因网络问题导致分组消失
                    // 直接更新UI显示
                    binding.tvSummary.text = "共 ${groups.size} 个小组，${getTotalStudents()} 名学生"
                    binding.tvTotalGroups.text = groups.size.toString()
                    setupGroupsList()
                    
                    // 延迟3秒后再重新加载数据，确保有足够时间让服务器处理完成
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadGroupsData()
                    }, 3000)
                } else {
                    val errorMsg = "随机分配失败: ${response.message ?: "未知错误"}"
                    Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TeacherViewActivity", errorMsg)
                }
            } catch (e: Exception) {
                    val errorMsg = "随机分配异常: ${e.message}"
                    Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TeacherViewActivity", errorMsg, e)
                }
        }
    }
    
    /**
     * 处理Activity返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_SELECT_QUESTION_TYPE && resultCode == RESULT_OK && data != null) {
            // 获取选中的题型
            val selectedQuestionType = data.getSerializableExtra("selectedQuestionType") as? QuestionType
            if (selectedQuestionType != null) {
                // 广播选中的题型
                broadcastQuestionType(gson.toJson(selectedQuestionType))
            }
        }
    }
    
    /**
      * 显示广播操作选项
      */
     private fun showBroadcastOptions() {
         val options = arrayOf("发送广播消息", "选择题型广播", "结束分组")
         AlertDialog.Builder(this)
             .setTitle("广播操作")
             .setItems(options) {
                 dialog, which ->
                 when (which) {
                     0 -> showBroadcastMessageDialog()
                     1 -> selectQuestionTypeForBroadcast()
                     2 -> endGroup()
                 }
             }
             .setNegativeButton("取消", null)
             .show()
     }
      
     /**
      * 结束分组，锁定分组状态
      */
     private fun endGroup() {
         val token = TokenManager.getToken() ?: ""
         if (token.isEmpty()) {
             Toast.makeText(this, "Token获取失败，无法执行结束分组操作", Toast.LENGTH_SHORT).show()
             return
         }
          
         // 显示加载提示
         Toast.makeText(this, "正在结束分组...", Toast.LENGTH_SHORT).show()
          
         activityScope.launch {
             try {
                 // 调用结束分组接口
                 val endGroupResponse = RetrofitClient.apiService.endGroup(
                     satoken = token,
                     subjectId = subjectId,
                     createdBy = teacherId
                 )
                  
                 if (endGroupResponse.code == 0) {
                     // 成功结束分组后，标记所有小组为已锁定
                     groups.forEach { group ->
                         group.isLocked = true
                     }
                      
                     // 更新UI，显示分组已结束
                     binding.tvSummary.text = "共 ${groups.size} 个小组，${getTotalStudents()} 名学生 (分组已结束)"
                      
                     // 向所有客户端广播分组结束的消息
                     broadcastGroupEndMessage()
                      
                     Toast.makeText(this@TeacherViewActivity, "分组已成功结束，学生不能再更改小组", Toast.LENGTH_SHORT).show()
                     Log.d("TeacherViewActivity", "分组已结束并锁定")
                 } else {
                     val errorMsg = "分组结束失败: ${endGroupResponse.message ?: "未知错误"}"
                     Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                     Log.e("TeacherViewActivity", errorMsg)
                 }
             } catch (e: Exception) {
                 val errorMsg = "分组结束异常: ${e.message}"
                 Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                 Log.e("TeacherViewActivity", errorMsg, e)
             }
         }
     }
      
     /**
      * 广播分组结束的消息给所有客户端
      */
     private fun broadcastGroupEndMessage() {
         if (isConnected && webSocket != null) {
             val broadcastData = mapOf(
                 "type" to "group_end",
                 "subjectId" to subjectId,
                 "teacherId" to teacherId
             )
             val jsonMessage = gson.toJson(broadcastData)
             webSocket?.send(jsonMessage)
             Log.d("TeacherViewActivity", "广播分组结束消息: $jsonMessage")
         } else {
             Log.d("TeacherViewActivity", "WebSocket未连接，无法广播分组结束消息")
         }
     }
      
     /**
      * 显示发送广播消息的对话框
      */
     private fun showBroadcastMessageDialog() {
         val builder = AlertDialog.Builder(this)
         builder.setTitle("发送广播消息")
          
         // 创建一个输入框
         val input = EditText(this)
         input.hint = "请输入要广播的消息内容"
          
         // 添加输入框到对话框
         builder.setView(input)
          
         // 设置确定按钮
         builder.setPositiveButton("发送") { dialog, which ->
             val message = input.text.toString().trim()
             if (message.isNotEmpty()) {
                 sendBroadcastMessage(message)
             } else {
                 Toast.makeText(this, "消息内容不能为空", Toast.LENGTH_SHORT).show()
             }
         }
          
         // 设置取消按钮
         builder.setNegativeButton("取消", null)
          
         // 显示对话框
         builder.show()
     }
      
     /**
      * 选择要广播的题型
      */
     private fun selectQuestionTypeForBroadcast() {
         // 启动题型列表Activity（选择模式）
         val intent = Intent(this, QuestionTypeListActivity::class.java)
         intent.putExtra("selectMode", true)
         startActivityForResult(intent, REQUEST_SELECT_QUESTION_TYPE)
     }
     
     // 常量已在文件顶部的伴生对象中定义
    
    /**
     * 发送广播消息
     */
    private fun sendBroadcastMessage(message: String) {
        if (isConnected && webSocket != null) {
            val broadcastData = mapOf(
                "type" to "broadcast",
                "content" to message,
                "teacherId" to teacherId,
                "subjectId" to subjectId
            )
            val jsonMessage = gson.toJson(broadcastData)
            webSocket?.send(jsonMessage)
            Toast.makeText(this, "广播消息已发送", Toast.LENGTH_SHORT).show()
            Log.d("GroupSeat", "发送广播消息: $jsonMessage")
        } else {
            Toast.makeText(this, "WebSocket未连接，无法发送广播消息", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 广播题型
     */
    private fun broadcastQuestionType(questionType: String) {
        if (isConnected && webSocket != null) {
            val questionData = mapOf(
                "type" to "question_type",
                "questionType" to questionType,
                "teacherId" to teacherId,
                "subjectId" to subjectId
            )
            val jsonMessage = gson.toJson(questionData)
            webSocket?.send(jsonMessage)
            Toast.makeText(this, "题型广播已发送", Toast.LENGTH_SHORT).show()
            Log.d("GroupSeat", "发送题型广播: $jsonMessage")
        } else {
            Toast.makeText(this, "WebSocket未连接，无法发送题型广播", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 连接WebSocket
     */
    private fun connectWebSocket() {
        // 获取最新的Token
        val tokenToUse = TokenManager.getToken() ?: ""
        if (tokenToUse.isEmpty()) {
            Log.e("TeacherViewActivity", "Token为空，无法连接WebSocket")
            Toast.makeText(this, "Token获取失败，无法建立WebSocket连接", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("TeacherViewActivity", "连接WebSocket，Token长度: ${tokenToUse.length}")

        // 构建完整的WebSocket连接地址
        // 优化teamId获取逻辑，确保URL格式正确
        val teamId = if (groups.isNotEmpty()) {
            groups[0].id.toString() // 使用第一个小组的id作为teamId
        } else {
            "default" // 使用"default"作为默认teamId，避免空字符串导致URL格式问题
        }
        
        // 构建基础URL，不包含token
        val wsUrl = "${wsBaseUrl}${teamId}"
        Log.d("TeacherViewActivity", "WebSocket连接URL: $wsUrl")
        
        try {
            // 创建OkHttpClient
            val client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build()
            
            // 创建WebSocket请求，将token放在请求头中而不是URL参数
            val request = Request.Builder()
                .url(wsUrl)
                .addHeader("satoken", tokenToUse)
                .build()
            
            // 建立WebSocket连接
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    mainHandler.post {
                        isConnected = true
                        Log.d("TeacherViewActivity", "WebSocket连接成功")
                        Toast.makeText(this@TeacherViewActivity, "WebSocket连接成功", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    mainHandler.post {
                        Log.d("TeacherViewActivity", "收到消息: $text")
                        // 处理收到的消息
                        // 检查是否收到未登录异常消息
                        if (text.contains("当前用户未登录") || text.contains("Token is empty")) {
                            Log.e("TeacherViewActivity", "WebSocket认证失败: $text")
                            // 断开当前连接
                            disconnectWebSocket()
                        }
                    }
                }
                
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    // 处理二进制消息
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    mainHandler.post {
                        isConnected = false
                        Log.d("TeacherViewActivity", "WebSocket连接关闭中: $code, $reason")
                    }
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    mainHandler.post {
                        isConnected = false
                        Log.d("TeacherViewActivity", "WebSocket连接已关闭: $code, $reason")
                    }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    mainHandler.post {
                        isConnected = false
                        Log.e("TeacherViewActivity", "WebSocket连接失败: ${t.message}")
                        Toast.makeText(this@TeacherViewActivity, "WebSocket连接失败: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            
            // 防止客户端退出
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e("TeacherViewActivity", "WebSocket连接异常: ${e.message}")
            Toast.makeText(this, "WebSocket连接异常: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 关闭WebSocket连接
     */
    private fun disconnectWebSocket() {
        if (webSocket != null) {
            webSocket?.close(1000, "主动关闭连接")
            webSocket = null
            isConnected = false
        }
    }
    private fun loadGroupsData() {
        // 显示加载提示
        Toast.makeText(this, "正在获取小组数据...", Toast.LENGTH_SHORT).show()
        
        // 添加日志，打印当前使用的subjectId和teacherId，帮助调试
        Log.d("TeacherViewActivity", "加载小组数据: subjectId=$subjectId, teacherId=$teacherId")
        
        // 尝试从API获取小组数据
        activityScope.launch {
            try {
                // 使用TokenManager获取token、subjectId和teacherId调用getGroups接口
                val token = TokenManager.getToken() ?: ""
                
                // 使用位置参数而非命名参数，避免参数名不匹配问题
                val groupsResponse = RetrofitClient.apiService.getGroups(token, subjectId, teacherId)
                
                if (groupsResponse.code == 0 && groupsResponse.data != null) {
                    val groupList = groupsResponse.data
                    if (!groupList!!.isEmpty()) {
                        // 清空现有小组数据
                        groups.clear()
                        
                        // 添加API返回的小组数据
                        groups.addAll(groupList)
                        
                        // 更新分组数量
                        groupNumber = groups.size
                        
                        // 计算预设学生总数（所有小组的capacity之和）
                        val totalCapacity = groups.sumOf { it.capacity }
                        // 设置预设学生总数到tvOnlineStudents
                        binding.tvAllStudents.text = totalCapacity.toString()
                        
                        // 更新UI显示
                        binding.tvSummary.text = "共 ${groups.size} 个小组，${getTotalStudents()} 名学生"
                        binding.tvTotalGroups.text = groups.size.toString()
                        setupGroupsList()
                    } else {
                        // 即使数据为空，也正常显示空列表
                        groups.clear()
                        groupNumber = 0
                        binding.tvAllStudents.text = "0"
                        binding.tvSummary.text = "共 ${groups.size} 个小组，${getTotalStudents()} 名学生"
                        binding.tvTotalGroups.text = groups.size.toString()
                        setupGroupsList()
                    }
                } else {
                    // 如果获取小组数据失败，使用默认数据
                    handleApiFailure()
                }
                
                // 小组数据加载完成后，建立WebSocket连接
                connectWebSocket()
            } catch (e: Exception) {
                // 发生异常，使用默认数据
                handleApiFailure()
                Log.e("GroupSeat", "获取小组数据异常: ${e.message}")
                
                // 即使发生异常，也尝试建立WebSocket连接
                connectWebSocket()
            }
        }
    }
    
    private fun handleApiFailure() {
        // API调用失败，使用默认数据
        binding.tvSummary.text = "API获取失败，使用本地数据"
        // 设置默认分组数量为4，确保有小组显示
        if (groupNumber == 0) {
            groupNumber = 4
        }
        //设置默认数据
        initData()
        // 更新tvTotalGroups显示为正确的分组数
        binding.tvTotalGroups.text = groups.size.toString()
        setupGroupsList()
    }

    private fun saveGroupNumberToServer(newGroupNumber: Int) {
        activityScope.launch {
            try {
                // 显示加载提示
                Toast.makeText(this@TeacherViewActivity, "正在保存分组大小...", Toast.LENGTH_SHORT).show()
                
                // 调用生成小组接口，传入subjectId和targetTeamCount
                try {
                    withContext(Dispatchers.IO) {
                        // 使用GenerateGroupDTO请求体代替Query参数
                        val requestBody = GenerateGroupDTO(
                            subjectId = subjectId,
                            targetTeamCount = newGroupNumber
                        )
                        
                        val token = TokenManager.getToken() ?: ""
                        // 根据ApiService接口定义，satoken应该作为HTTP头参数传递
                        val response = RetrofitClient.apiService.generateGroups(
                            satoken = token,
                            request = requestBody
                        )
                        
                        // 检查响应是否成功 - 只检查业务逻辑code是否为0
                        Log.d("TeacherViewActivity", "generateGroups响应: code=${response.code}, message=${response.message}")
                        if (response.code != 0) {
                            throw Exception("服务器返回错误: ${response.message ?: "未知错误"} (code=${response.code}) ")
                        }
                    }
                    
                    // API调用成功后，更新分组数并重新加载数据
                    groupNumber = newGroupNumber
                    Toast.makeText(this@TeacherViewActivity, "分组大小设置成功", Toast.LENGTH_SHORT).show()
                    
                    // 立即重新加载分组数据，确保本地数据与服务器数据同步
                    loadGroupsData()
                } catch (e: Exception) {
                    // API调用失败，仍然更新本地数据
                    groupNumber = newGroupNumber
                    // 更新本地groups列表和UI
                    initData()
                    binding.tvSummary.text = "共 " + groups.size + " 个小组，" + getTotalStudents() + " 名学生"
                    binding.tvTotalGroups.text = newGroupNumber.toString()
                    binding.tvAllStudents.text = getTotalStudents().toString()
                    setupGroupsList()
                    
                    val errorMsg = "保存失败，但已更新本地数据: " + e.message
                    Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TeacherViewActivity", errorMsg, e)
                    
                    // 特别处理分组数错误情况
                    if (e.message?.contains("code=201") == true || e.message?.contains("选择目标队伍数量") == true) {
                        // 显示更友好的提示，建议用户重新选择分组数
                        Toast.makeText(this@TeacherViewActivity, "服务器不接受该分组数量，请尝试选择其他数字", Toast.LENGTH_LONG).show()
                        // 打开设置分组数对话框，让用户重新选择
                        showSetGroupSizeDialog()
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "保存失败: " + e.message
                Toast.makeText(this@TeacherViewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                Log.e("TeacherViewActivity", errorMsg, e)
            }
        }
    }

    private fun showSetGroupSizeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("设置分组数")
        
        // 创建一个输入框
        val input = EditText(this)
        input.hint = "请输入小组数目"
        input.setText(groupNumber.toString())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        // 添加输入框到对话框
        builder.setView(input)
        
        // 设置确定按钮
        builder.setPositiveButton("确定") { dialog, which ->
            val newGroupNumber = input.text.toString().toIntOrNull()
            if(newGroupNumber!=null){
                saveGroupNumberToServer(newGroupNumber)    //保存到后端
            }else{
                showSetGroupSizeDialog()
            }
        }
        
        // 设置取消按钮
        builder.setNegativeButton("取消", null)
        
        // 显示对话框
        builder.show()
    }

    private fun initData() {
        // 模拟小组数据
        val student1 = Student(1, "张三", null, false, 0)
        val student2 = Student(2, "李四", null, false, 1)
        val student3 = Student(3, "王五", null, false, 2)
        val student4 = Student(4, "赵六", null, false, 0)

        groups.clear()
        // 根据groupNumber创建对应的小组数量
        for (i in 1..groupNumber) {
            groups.add(Group(i, "第${i}小组", 3, 1, mutableListOf(null, null, null)))
        }
        
        // 为前几个小组添加一些学生，使数据看起来更真实
        if (groups.isNotEmpty()) {
            groups[0].students[0] = student1
        }
        if (groups.size > 1) {
            groups[1].students[1] = student2
        }
        if (groups.size > 2) {
            groups[2].students[2] = student3
        }
        if (groups.size > 3) {
            groups[3].students[0] = student4
        }
    }

    private fun setupGroupsList() {
         binding.rvGroups.layoutManager = LinearLayoutManager(this)
         binding.rvGroups.adapter = GroupsAdapter(groups) { group ->
             val intent = Intent(this, DiscussionActivity::class.java)
             intent.putExtra("groupId", group.id)
             intent.putExtra("groupName", group.name)
             intent.putExtra("isTeacher", true)
             // 将students集合转换为JSON字符串后传递
             try {
                 val gson = com.google.gson.Gson()
                 val studentsJson = gson.toJson(group.students)
                 Log.d("TeacherViewActivity", "Failed to convert students to JSON: ${studentsJson}")
                 intent.putExtra("groupStudentsJson", studentsJson)
             } catch (e: Exception) {
                 Log.e("TeacherViewActivity", "Failed to convert students to JSON: ${e.message}")
             }
             startActivity(intent)
         }
     }

    private fun getTotalStudents(): Int {
        return groups.sumOf { group ->
            group.students.count { it != null } // 只计算非空学生
        }
    }
}


//小组监控面板的小组recyclerview适配器
class GroupsAdapter(
    private val groups: List<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        val tvMemberCount: TextView = itemView.findViewById(R.id.tvMemberCount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        // 使用LayoutInflater并设置正确的LayoutParams，确保布局能够正确显示
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_teacher_group, parent, false
        )
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        val filledCount = group.students.count { it != null } // 只计算非空学生
        val totalCount = group.capacity

        holder.tvGroupName.text = group.name
        holder.tvMemberCount.text = "$filledCount/$totalCount 人"

        when {
            filledCount == totalCount -> {
                holder.tvStatus.text = "已满员"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
            }
            filledCount==0->{
                holder.tvStatus.text = "待分组"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            else -> {
                holder.tvStatus.text = "进行中"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_in_progress)
            }
        }

        holder.itemView.setOnClickListener {
            onGroupClick(group)
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }
}