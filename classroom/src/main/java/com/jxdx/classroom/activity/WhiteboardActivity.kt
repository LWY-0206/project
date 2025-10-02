package com.jxdx.classroom.activity

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityWhiteboardBinding
import com.jxdx.classroom.model.Message
import com.jxdx.classroom.model.UserInfo
import com.jxdx.classroom.model.WhiteboardSnapshot
import com.jxdx.classroom.websocket.WhiteboardWebSocketManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jxdx.classroom.adapter.WhiteboardSnapshotAdapter
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 白板绘制Activity
 * 提供绘制、橡皮擦、清空、提交等功能
 */
class WhiteboardActivity : BaseActivity<ActivityWhiteboardBinding>() {
    
    private val TAG = "WhiteboardActivity"
    
    // 当前选中的颜色
    private var currentColor = Color.BLACK
    
    // 当前是否为橡皮擦模式
    private var isEraserMode = false
    
    // 房间ID
    private var roomId: String = ""
    
    // ViewModel
    private lateinit var viewModel: WhiteboardViewModel
    
    // WebSocket管理器
    private lateinit var webSocketManager: WhiteboardWebSocketManager
    
    // 当前用户信息
    private var currentUserInfo: UserInfo? = null
    
    // 白板快照列表（教师端使用）
    private val whiteboardSnapshots = mutableListOf<WhiteboardSnapshot>()
    private lateinit var snapshotAdapter: WhiteboardSnapshotAdapter

    override fun bindLayout(): ActivityWhiteboardBinding {
        return ActivityWhiteboardBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 获取传递的roomId参数
        roomId = intent.getStringExtra("roomId") ?: ""
        Log.d(TAG, "接收到roomId: $roomId")
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[WhiteboardViewModel::class.java]
        
        // 初始化WebSocket管理器
        webSocketManager = WhiteboardWebSocketManager()
        
        setupToolbar()
        setupColorPickers()
        setupStrokeWidthSeekBar()
        setupToolButtons()
        setupSnapshotRecyclerView()
        
        // 获取用户信息并连接WebSocket
        getUserInfoAndConnect()
    }

    override fun subscribeUi() {
        // 初始化白板设置
        view.whiteboardView.setPaintColor(currentColor)
        view.whiteboardView.setStrokeWidth(5f)
        
        // 监听用户信息获取结果
        viewModel.userInfoLiveData.observe(this) { result ->
            result.onSuccess { userInfo ->
                currentUserInfo = userInfo
                Log.d(TAG, "获取用户信息成功: ${userInfo?.userName}, 身份: ${userInfo?.getIdentityDescription()}")
                
                // 根据用户身份设置界面
                userInfo?.let { setupUIForUserRole(it) }
                
                // 连接WebSocket
                connectWebSocket()
            }
            
            result.onError { error, _ ->
                Log.e(TAG, "获取用户信息失败: ${error?.message}")
                Toast.makeText(this, "获取用户信息失败: ${error?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 监听通用上传结果
        viewModel.uploadLiveData.observe(this) { result ->
            result.onSuccess { imageUrls ->
                if (imageUrls != null && imageUrls.isNotEmpty()) {
                    val imageUrl = imageUrls[0] // 获取第一个图片URL
                    Log.d(TAG, "白板图片上传成功，URL: $imageUrl")
                    
                    // 通过WebSocket发送白板快照
                    sendWhiteboardSnapshot(imageUrl)
                    
                    // 显示提交成功提示，让学生手动返回
                    Toast.makeText(this, "白板快照已提交成功！\n请点击返回按钮退出", Toast.LENGTH_LONG).show()
                    
                    // 禁用提交按钮，避免重复提交
                    view.btnSubmit.isEnabled = false
                    view.btnSubmit.text = "已提交"
                    
                    // 不自动关闭Activity，让学生手动点击返回按钮
                    // 这样可以保持WebSocket连接，确保教师端能接收到消息
                    
                } else {
                    Toast.makeText(this, "上传失败，未获取到图片URL", Toast.LENGTH_SHORT).show()
                }
            }
            
            result.onError { error, _ ->
                Log.e(TAG, "白板图片上传失败: ${error?.message}")
                Toast.makeText(this, "上传失败: ${error?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
    }

    /**
     * 设置顶部工具栏
     */
    private fun setupToolbar() {
        // 返回按钮
        view.btnBack.setOnClickListener {
            finish()
        }
        
        // 提交按钮
        view.btnSubmit.setOnClickListener {
            submitWhiteboard()
        }
    }

    /**
     * 设置颜色选择器
     */
    private fun setupColorPickers() {
        // 黑色
        view.colorBlack.setOnClickListener {
            selectColor(Color.BLACK, view.colorBlack)
        }
        
        // 红色
        view.colorRed.setOnClickListener {
            selectColor(Color.parseColor("#F44336"), view.colorRed)
        }
        
        // 蓝色
        view.colorBlue.setOnClickListener {
            selectColor(Color.parseColor("#2196F3"), view.colorBlue)
        }
        
        // 绿色
        view.colorGreen.setOnClickListener {
            selectColor(Color.parseColor("#4CAF50"), view.colorGreen)
        }
        
        // 默认选中黑色
        selectColor(Color.BLACK, view.colorBlack)
    }

    /**
     * 选择颜色
     */
    private fun selectColor(color: Int, colorView: android.view.View) {
        currentColor = color
        isEraserMode = false
        
        // 重置所有颜色按钮的边框
        resetColorSelection()
        
        // 设置选中状态
        colorView.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            setStroke(4, Color.GRAY)
        }
        
        // 更新白板画笔
        view.whiteboardView.setPaintColor(color)
        view.whiteboardView.setEraserMode(false)
        
        // 更新橡皮擦按钮状态
        updateEraserButtonState()
    }

    /**
     * 重置颜色选择状态
     */
    private fun resetColorSelection() {
        view.colorBlack.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.BLACK)
        }
        view.colorRed.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#F44336"))
        }
        view.colorBlue.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#2196F3"))
        }
        view.colorGreen.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#4CAF50"))
        }
    }

    /**
     * 设置画笔粗细滑块
     */
    private fun setupStrokeWidthSeekBar() {
        view.strokeWidthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val strokeWidth = (progress + 1) * 2f // 2-42的范围
                    view.whiteboardView.setStrokeWidth(strokeWidth)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * 设置工具按钮
     */
    private fun setupToolButtons() {
        // 橡皮擦按钮
        view.btnEraser.setOnClickListener {
            toggleEraserMode()
        }
        
        // 清空按钮
        view.btnClear.setOnClickListener {
            showClearConfirmDialog()
        }
    }

    /**
     * 切换橡皮擦模式
     */
    private fun toggleEraserMode() {
        isEraserMode = !isEraserMode
        view.whiteboardView.setEraserMode(isEraserMode)
        updateEraserButtonState()
        
        if (isEraserMode) {
            resetColorSelection()
        }
    }

    /**
     * 更新橡皮擦按钮状态
     */
    private fun updateEraserButtonState() {
        val cardView = view.btnEraser.parent as androidx.cardview.widget.CardView
        if (isEraserMode) {
            cardView.setCardBackgroundColor(Color.parseColor("#FF6B35"))
            view.btnEraser.setTextColor(Color.WHITE)
        } else {
            cardView.setCardBackgroundColor(Color.WHITE)
            view.btnEraser.setTextColor(Color.parseColor("#666666"))
        }
    }

    /**
     * 显示清空确认对话框
     */
    private fun showClearConfirmDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("清空画板")
            .setMessage("确定要清空整个画板吗？此操作不可撤销。")
            .setPositiveButton("确定") { _, _ ->
                view.whiteboardView.clearCanvas()
                Toast.makeText(this, "画板已清空", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 提交白板内容
     */
    private fun submitWhiteboard() {
        try {
            // 获取白板内容为Bitmap
            val bitmap = view.whiteboardView.getCanvasBitmap()
            Log.d(TAG, "白板内容已获取，Bitmap尺寸: ${bitmap.width}x${bitmap.height}")
            
            // 将Bitmap保存为临时文件
            val filePath = saveBitmapToFile(bitmap)
            if (filePath != null) {
                Log.d(TAG, "使用通用上传接口上传白板图片")
                Toast.makeText(this, "正在上传白板图片...", Toast.LENGTH_SHORT).show()
                viewModel.uploadWhiteboardImage(filePath)
            } else {
                Toast.makeText(this, "保存图片失败，请重试", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "提交白板内容失败: ${e.message}")
            Toast.makeText(this, "提交失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 将Bitmap保存为临时文件
     */
    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        return try {
            // 创建临时文件目录
            val cacheDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (cacheDir == null) {
                Log.e(TAG, "无法获取缓存目录")
                return null
            }
            
            // 生成唯一文件名
            val fileName = "whiteboard_${UUID.randomUUID()}.jpg"
            val file = File(cacheDir, fileName)
            
            // 压缩并保存Bitmap
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 80%质量
            outputStream.flush()
            outputStream.close()
            
            Log.d(TAG, "白板图片已保存到: ${file.absolutePath}")
            file.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "保存Bitmap失败: ${e.message}")
            null
        }
    }

    /**
     * 获取用户信息并连接WebSocket
     */
    private fun getUserInfoAndConnect() {
        Log.d(TAG, "开始获取用户信息...")
        viewModel.getUserInfo()
    }
    
    /**
     * 根据用户身份设置界面
     */
    private fun setupUIForUserRole(userInfo: UserInfo) {
        if (userInfo.isTeacher()) {
            // 教师端：显示快照面板，隐藏绘制工具栏
            view.snapshotPanel.visibility = android.view.View.VISIBLE
            view.drawToolbar.visibility = android.view.View.GONE
            view.btnSubmit.visibility = android.view.View.VISIBLE
            view.btnSubmit.text = "管理白板"
            view.btnSubmit.setOnClickListener {
                // 教师端不需要提交功能，只是管理界面
                Toast.makeText(this, "教师端白板管理界面", Toast.LENGTH_SHORT).show()
            }
            view.tvTitle.text = "白板管理 - 教师端"
            Log.d(TAG, "教师端界面已设置")
        } else {
            // 学生端：隐藏快照面板，显示绘制工具栏
            view.snapshotPanel.visibility = android.view.View.GONE
            view.drawToolbar.visibility = android.view.View.VISIBLE
            view.btnSubmit.visibility = android.view.View.VISIBLE
            view.btnSubmit.text = "提交"
            view.btnSubmit.setOnClickListener {
                submitWhiteboard()
            }
            view.tvTitle.text = "白板绘制 - 学生端"
            Log.d(TAG, "学生端界面已设置")
        }
    }
    
    /**
     * 连接WebSocket
     */
    private fun connectWebSocket() {
        currentUserInfo?.let { userInfo ->
            Log.d(TAG, "开始连接WebSocket，房间ID: $roomId, 身份: ${userInfo.identity}")
            Log.d(TAG, "用户信息: ID=${userInfo.id}, 姓名=${userInfo.userName}")
            
            // 检查必要参数
            if (roomId.isEmpty()) {
                Log.e(TAG, "房间ID为空，无法连接WebSocket")
                Toast.makeText(this, "房间ID无效，请重新进入", Toast.LENGTH_LONG).show()
                return
            }
            
            // 设置WebSocket监听器
            webSocketManager.setMessageListener(object : WhiteboardWebSocketManager.MessageListener {
                override fun onMessageReceived(message: Message) {
                    Log.d(TAG, "=== 教师端收到消息 ===")
                    Log.d(TAG, "消息ID: ${message.id}")
                    Log.d(TAG, "消息内容: ${message.content}")
                    Log.d(TAG, "消息类型: ${message.messageType}")
                    Log.d(TAG, "发送者ID: ${message.senderId}")
                    Log.d(TAG, "发送者姓名: ${message.senderName}")
                    Log.d(TAG, "发送者类型: ${message.senderType}")
                    Log.d(TAG, "图片URL: ${message.imageUrl}")
                    Log.d(TAG, "时间戳: ${message.timestamp}")
                    
                    runOnUiThread {
                        if (message.content != null) {
                            Toast.makeText(this@WhiteboardActivity, "收到消息: ${message.content}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@WhiteboardActivity, "收到消息: [内容为空]", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                override fun onWhiteboardSnapshotReceived(imageUrl: String, senderId: String, senderName: String) {
                    Log.d(TAG, "=== 教师端收到白板快照 ===")
                    Log.d(TAG, "图片URL: $imageUrl")
                    Log.d(TAG, "发送者ID: $senderId")
                    Log.d(TAG, "发送者姓名: $senderName")
                    Log.d(TAG, "当前用户身份: ${currentUserInfo?.getIdentityDescription()}")
                    Log.d(TAG, "是否为教师: ${currentUserInfo?.isTeacher()}")
                    
                    runOnUiThread {
                        Toast.makeText(this@WhiteboardActivity, "收到学生白板快照！", Toast.LENGTH_SHORT).show()
                        
                        // 如果是教师端，显示白板快照
                        if (currentUserInfo?.isTeacher() == true) {
                            Log.d(TAG, "开始处理教师端白板快照显示")
                            addWhiteboardSnapshot(imageUrl, senderId, senderName)
                            // 在教师端主白板区域显示学生内容
                            displayStudentWhiteboard(imageUrl, senderId)
                        } else {
                            Log.d(TAG, "当前用户不是教师，不处理白板快照")
                        }
                    }
                }
                
                override fun onError(error: String) {
                    Log.e(TAG, "WebSocket错误: $error")
                    runOnUiThread {
                        Toast.makeText(this@WhiteboardActivity, "WebSocket错误: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            
            // 设置连接状态监听器
            webSocketManager.setConnectionListener(object : WhiteboardWebSocketManager.ConnectionListener {
                override fun onConnected() {
                    Log.d(TAG, "WebSocket连接成功")
                    runOnUiThread {
                        Toast.makeText(this@WhiteboardActivity, "已连接到白板房间", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onDisconnected() {
                    Log.d(TAG, "WebSocket连接断开")
                    runOnUiThread {
                        Toast.makeText(this@WhiteboardActivity, "白板连接已断开", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onConnectionError(error: String) {
                    Log.e(TAG, "WebSocket连接错误: $error")
                    Log.e(TAG, "连接参数 - 房间ID: $roomId, 身份: ${userInfo.identity}, 用户ID: ${userInfo.id}")
                    runOnUiThread {
                        Toast.makeText(this@WhiteboardActivity, "连接失败: $error", Toast.LENGTH_LONG).show()
                    }
                }
            })
            
            // 连接WebSocket（统一标准：0=学生，1=老师）
            val whiteboardIdentity = if (userInfo.isTeacher()) 1 else 0
            Log.d(TAG, "用户身份转换：外部身份=${userInfo.identity} -> 白板身份=$whiteboardIdentity")
            webSocketManager.connect(roomId, whiteboardIdentity)
        }
    }
    
    /**
     * 发送白板快照
     */
    private fun sendWhiteboardSnapshot(imageUrl: String) {
        currentUserInfo?.let { userInfo ->
            Log.d(TAG, "=== 学生端发送白板快照 ===")
            Log.d(TAG, "图片URL: $imageUrl")
            Log.d(TAG, "发送者ID: ${userInfo.id}")
            Log.d(TAG, "发送者姓名: ${userInfo.userName}")
            Log.d(TAG, "用户身份: ${userInfo.getIdentityDescription()}")
            Log.d(TAG, "房间ID: $roomId")
            Log.d(TAG, "WebSocket连接状态: ${webSocketManager.isConnected()}")
            
            if (webSocketManager.isConnected()) {
                webSocketManager.sendWhiteboardSnapshot(
                    imageUrl = imageUrl,
                    senderId = userInfo.id.toString(),
                    senderName = userInfo.userName
                )
                Log.d(TAG, "白板快照已发送: $imageUrl")
                Log.d(TAG, "WebSocket连接状态: ${webSocketManager.isConnected()}")
                Toast.makeText(this, "白板快照已发送到教师端", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "WebSocket未连接，无法发送白板快照")
                Toast.makeText(this, "连接已断开，无法发送白板快照", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e(TAG, "用户信息为空，无法发送白板快照")
            Toast.makeText(this, "用户信息获取失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 发送文本消息
     */
    private fun sendTextMessage(content: String) {
        currentUserInfo?.let { userInfo ->
            webSocketManager.sendTextMessage(
                content = content,
                senderId = userInfo.id.toString(),
                senderName = userInfo.userName
            )
            Log.d(TAG, "文本消息已发送: $content")
        }
    }
    
    /**
     * 设置白板快照RecyclerView（教师端使用）
     */
    private fun setupSnapshotRecyclerView() {
        snapshotAdapter = WhiteboardSnapshotAdapter(
            snapshots = whiteboardSnapshots,
            onItemClick = { snapshot ->
                // 点击快照时的处理
                showSnapshotDialog(snapshot)
            }
        )
        
        // 设置RecyclerView
        view.rvWhiteboardSnapshots.layoutManager = LinearLayoutManager(this)
        view.rvWhiteboardSnapshots.adapter = snapshotAdapter
    }
    
    /**
     * 添加白板快照到列表（教师端使用）
     */
    private fun addWhiteboardSnapshot(imageUrl: String, senderId: String, senderName: String) {
        val snapshot = WhiteboardSnapshot(
            id = System.currentTimeMillis().toString(),
            studentId = senderId,
            studentName = senderName, // 使用从WebSocket消息中传来的真实学生姓名
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            isViewed = false
        )
        
        whiteboardSnapshots.add(0, snapshot) // 添加到列表顶部
        snapshotAdapter.updateSnapshots(whiteboardSnapshots)
        
        Log.d(TAG, "已添加白板快照: ${snapshot.studentName}")
    }
    
    
    /**
     * 显示白板快照对话框
     */
    private fun showSnapshotDialog(snapshot: WhiteboardSnapshot) {
        // 标记为已查看
        val index = whiteboardSnapshots.indexOfFirst { it.id == snapshot.id }
        if (index != -1) {
            whiteboardSnapshots[index] = snapshot.copy(isViewed = true)
            snapshotAdapter.updateSnapshots(whiteboardSnapshots)
        }
        
        // 在教师端主白板区域显示学生内容
        displayStudentWhiteboard(snapshot.imageUrl, snapshot.studentId)
        
        // 显示大图对话框
        android.app.AlertDialog.Builder(this)
            .setTitle("${snapshot.studentName} 的白板")
            .setMessage("提交时间: ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(snapshot.timestamp))}")
            .setPositiveButton("确定", null)
            .show()
    }
    
    /**
     * 在教师端主白板区域显示学生白板内容
     */
    private fun displayStudentWhiteboard(imageUrl: String, studentId: String) {
        if (currentUserInfo?.isTeacher() != true) return
        
        // 清空当前白板内容
        view.whiteboardView.clearCanvas()
        
        // 使用Glide加载图片到白板
        com.bumptech.glide.Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(
                    resource: android.graphics.Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                ) {
                    // 将学生白板内容绘制到教师端白板上
                    view.whiteboardView.drawBitmap(resource)
                    Log.d(TAG, "已显示学生 $studentId 的白板内容")
                }
                
                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // 清理资源
                }
                
                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    Log.e(TAG, "加载学生白板图片失败: $imageUrl")
                    Toast.makeText(this@WhiteboardActivity, "加载学生白板失败", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onBackPressed() {
        // 检查是否有内容未保存
        android.app.AlertDialog.Builder(this)
            .setTitle("退出白板")
            .setMessage("确定要退出吗？未提交的内容将丢失。")
            .setPositiveButton("退出") { _, _ ->
                // 断开WebSocket连接
                webSocketManager.disconnect()
                super.onBackPressed()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 释放WebSocket资源
        webSocketManager.release()
    }
}
