package com.jxdx.classroom.widget

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import com.jxdx.classroom.R
import com.jxdx.classroom.activity.WhiteboardActivity

/**
 * 悬浮球管理器
 */
class FloatingBallManager(private val context: Context) {
    
    private val TAG = "FloatingBallManager"
    
    private var windowManager: WindowManager? = null
    private var floatingBallView: View? = null
    private var popupWindow: PopupWindow? = null
    private var isShowing = false
    private var layoutParams: WindowManager.LayoutParams? = null
    
    // 拖拽相关变量
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    
    // 房间ID
    private var roomId: String = ""
    
    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    /**
     * 显示悬浮球
     */
    fun showFloatingBall(roomId: String) {
        this.roomId = roomId
        
        if (isShowing) {
            Log.w(TAG, "悬浮球已经在显示中")
            return
        }
        
        // 检查悬浮窗权限
        if (!checkFloatingPermission()) {
            requestFloatingPermission()
            return
        }
        
        createFloatingBall()
        isShowing = true
        Log.d(TAG, "悬浮球显示成功")
    }
    
    /**
     * 隐藏悬浮球
     */
    fun hideFloatingBall() {
        if (!isShowing) {
            return
        }
        
        try {
            windowManager?.removeView(floatingBallView)
            floatingBallView = null
            isShowing = false
            Log.d(TAG, "悬浮球隐藏成功")
        } catch (e: Exception) {
            Log.e(TAG, "隐藏悬浮球失败", e)
        }
    }
    
    /**
     * 创建悬浮球
     */
    private fun createFloatingBall() {
        val inflater = LayoutInflater.from(context)
        floatingBallView = inflater.inflate(R.layout.floating_ball, null)
        
        val floatingBall = floatingBallView?.findViewById<ImageView>(R.id.floating_ball)
        
        // 设置悬浮球参数
        layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START  // 改为TOP|START，允许自由移动
            x = 50
            y = 200
        }
        
        // 设置触摸监听器（处理拖拽和点击）
        floatingBallView?.setOnTouchListener { view, event ->
            handleTouchEvent(event)
        }
        
        try {
            windowManager?.addView(floatingBallView, layoutParams)
        } catch (e: Exception) {
            Log.e(TAG, "添加悬浮球失败", e)
        }
    }
    
    /**
     * 显示功能菜单
     */
    private fun showFunctionMenu() {
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.floating_ball_menu, null)
        
        // 创建PopupWindow
        popupWindow = PopupWindow(
            menuView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        
        // 设置菜单项点击事件
        menuView.findViewById<LinearLayout>(R.id.menu_whiteboard).setOnClickListener {
            openWhiteboardActivity()
            popupWindow?.dismiss()
        }
        
        menuView.findViewById<LinearLayout>(R.id.menu_view_students).setOnClickListener {
            Toast.makeText(context, "查看学生列表", Toast.LENGTH_SHORT).show()
            popupWindow?.dismiss()
        }
        
        menuView.findViewById<LinearLayout>(R.id.menu_quiz).setOnClickListener {
            openQuizActivity()
            popupWindow?.dismiss()
        }
        
        menuView.findViewById<LinearLayout>(R.id.menu_attendance).setOnClickListener {
            openAttendanceActivity()
            popupWindow?.dismiss()
        }
        
        menuView.findViewById<LinearLayout>(R.id.menu_close).setOnClickListener {
            hideFloatingBall()
            popupWindow?.dismiss()
        }
        
        // 显示菜单
        floatingBallView?.let { anchor ->
            popupWindow?.showAsDropDown(anchor, -200, -50)
        }
    }
    
    /**
     * 打开白板管理界面
     */
    private fun openWhiteboardActivity() {
        val intent = Intent(context, WhiteboardActivity::class.java).apply {
            putExtra("roomId", roomId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * 打开答题界面
     */
    private fun openQuizActivity() {
        // TODO: 实现答题功能
        Toast.makeText(context, "答题功能开发中...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "打开答题界面，房间ID: $roomId")
    }
    
    /**
     * 打开签到界面
     */
    private fun openAttendanceActivity() {
        // TODO: 实现签到功能
        Toast.makeText(context, "签到功能开发中...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "打开签到界面，房间ID: $roomId")
    }
    
    /**
     * 检查悬浮窗权限
     */
    private fun checkFloatingPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * 请求悬浮窗权限
     */
    private fun requestFloatingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 处理触摸事件
     */
    private fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录初始位置
                initialX = layoutParams?.x ?: 0
                initialY = layoutParams?.y ?: 0
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                Log.d(TAG, "触摸开始: initialX=$initialX, initialY=$initialY")
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                // 计算移动距离
                val deltaX = (event.rawX - initialTouchX).toInt()
                val deltaY = (event.rawY - initialTouchY).toInt()
                
                // 如果移动距离超过阈值，开始拖拽
                if (!isDragging && (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5)) {
                    isDragging = true
                    Log.d(TAG, "开始拖拽")
                }
                
                if (isDragging) {
                    // 更新悬浮球位置
                    val newX = initialX + deltaX
                    val newY = initialY + deltaY
                    
                    // 限制在屏幕范围内
                    val screenWidth = context.resources.displayMetrics.widthPixels
                    val screenHeight = context.resources.displayMetrics.heightPixels
                    val ballSize = 60 // 悬浮球大小
                    
                    val clampedX = newX.coerceIn(0, screenWidth - ballSize)
                    val clampedY = newY.coerceIn(0, screenHeight - ballSize)
                    
                    layoutParams?.x = clampedX
                    layoutParams?.y = clampedY
                    
                    Log.d(TAG, "移动悬浮球到: x=$clampedX, y=$clampedY")
                    
                    try {
                        windowManager?.updateViewLayout(floatingBallView, layoutParams)
                    } catch (e: Exception) {
                        Log.e(TAG, "更新悬浮球位置失败", e)
                    }
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "触摸结束，拖拽状态: $isDragging")
                
                // 如果没有拖拽，则显示菜单
                if (!isDragging) {
                    showFunctionMenu()
                }
                
                // 拖拽结束，重置状态
                isDragging = false
                return true
            }
            
            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "触摸取消")
                isDragging = false
                return true
            }
        }
        return false
    }
    
    /**
     * 自动吸附到屏幕边缘
     */
    private fun autoSnapToEdge() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val ballSize = 60
        
        layoutParams?.let { params ->
            val centerX = screenWidth / 2
            val newX = if (params.x < centerX) 0 else screenWidth - ballSize
            
            params.x = newX
            
            try {
                windowManager?.updateViewLayout(floatingBallView, params)
            } catch (e: Exception) {
                Log.e(TAG, "自动吸附失败", e)
            }
        }
    }
    
    /**
     * 是否正在显示
     */
    fun isShowing(): Boolean = isShowing
}
