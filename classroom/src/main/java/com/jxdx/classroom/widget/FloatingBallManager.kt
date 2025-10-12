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
import com.jxdx.classroom.group.WhiteboardDrawActivity
import com.jxdx.classroom.activity.ResourceSelectionActivity

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
        Log.d(TAG, "showFloatingBall被调用，roomId: $roomId")
        this.roomId = roomId
        
        if (isShowing) {
            Log.w(TAG, "悬浮球已经在显示中")
            return
        }
        
        // 检查悬浮窗权限
        if (!checkFloatingPermission()) {
            Log.w(TAG, "没有悬浮窗权限，请求权限")
            requestFloatingPermission()
            return
        }
        
        Log.d(TAG, "权限检查通过，开始创建悬浮球")
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
        Log.d(TAG, "开始创建悬浮球")
        
        val inflater = LayoutInflater.from(context)
        floatingBallView = inflater.inflate(R.layout.floating_ball, null)
        Log.d(TAG, "悬浮球视图创建成功: $floatingBallView")
        
        val floatingBall = floatingBallView?.findViewById<ImageView>(R.id.floating_ball)
        Log.d(TAG, "悬浮球ImageView: $floatingBall")
        
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
        Log.d(TAG, "悬浮球参数设置完成")
        
        // 设置触摸监听器（处理拖拽和点击）
        floatingBallView?.setOnTouchListener { view, event ->
            Log.d(TAG, "触摸事件被触发: ${event.action}")
            handleTouchEvent(event)
        }
        
        // 移除ImageView的点击监听器，避免与拖拽冲突
        // 拖拽和点击都通过OnTouchListener处理
        
        Log.d(TAG, "触摸监听器设置完成")
        
        try {
            windowManager?.addView(floatingBallView, layoutParams)
            Log.d(TAG, "悬浮球添加到窗口管理器成功")
        } catch (e: Exception) {
            Log.e(TAG, "添加悬浮球失败", e)
        }
    }
    
    /**
     * 显示功能菜单
     */
    private fun showFunctionMenu() {
        Log.d(TAG, "开始显示功能菜单")
        
        try {
            // 创建菜单视图
            val menuView = LayoutInflater.from(context).inflate(R.layout.floating_ball_menu, null)
            Log.d(TAG, "菜单视图创建成功")
            
            // 创建PopupWindow
            popupWindow = PopupWindow(
                menuView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            Log.d(TAG, "PopupWindow创建成功")
            
            // 设置背景和属性
            popupWindow?.setBackgroundDrawable(context.getDrawable(R.drawable.menu_background))
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            
            // 计算显示位置
            val floatingBall = floatingBallView?.findViewById<ImageView>(R.id.floating_ball)
            val location = IntArray(2)
            floatingBall?.getLocationOnScreen(location)
            
            val screenWidth = context.resources.displayMetrics.widthPixels
            val screenHeight = context.resources.displayMetrics.heightPixels
            
            // 计算偏移量，确保菜单不会超出屏幕
            var xOffset = 70
            var yOffset = 70
            
            if (location[0] + xOffset + 200 > screenWidth) {
                xOffset = -200 // 显示在左侧
            }
            if (location[1] + yOffset + 300 > screenHeight) {
                yOffset = -300 // 显示在上方
            }
            
            Log.d(TAG, "准备显示菜单，anchor: $floatingBallView")
            Log.d(TAG, "显示菜单 - 悬浮球位置: (${location[0]}, ${location[1]}), 偏移: ($xOffset, $yOffset)")
            
            // 显示菜单
            popupWindow?.showAsDropDown(floatingBallView, xOffset, yOffset)
            Log.d(TAG, "菜单显示成功")
            
            // 设置菜单项点击事件
            setupMenuClickListeners(menuView)
            
        } catch (e: Exception) {
            Log.e(TAG, "显示功能菜单失败", e)
            Toast.makeText(context, "显示菜单失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 设置菜单项点击事件
     */
    private fun setupMenuClickListeners(menuView: View) {
        try {
            val menuItems = listOf<Pair<Int, String>>(
                R.id.menu_whiteboard to "白板管理",
                R.id.menu_view_students to "小组列表", 
                R.id.menu_quiz to "工具",
                R.id.menu_attendance to "资料",
                R.id.menu_close to "关闭"
            )
            
            menuItems.forEach { (id: Int, name: String) ->
                menuView.findViewById<View>(id)?.setOnClickListener {
                    Log.d(TAG, "点击了菜单项: $name")
                    
                    when (name) {
                        "白板管理" -> openWhiteboardActivity()
                        "小组列表" -> openGroupListActivity()
                        "工具" -> openMathToolActivity()
                        "资料" -> openAttendanceActivity()
                        "关闭" -> hideFloatingBall()
                    }
                    
                    popupWindow?.dismiss()
                }
            }
            
            Log.d(TAG, "菜单项点击事件设置完成")
        } catch (e: Exception) {
            Log.e(TAG, "设置菜单点击事件失败", e)
        }
    }
    
    /**
     * 打开白板管理界面
     */
    private fun openWhiteboardActivity() {
        val intent = Intent(context, WhiteboardDrawActivity::class.java).apply {
            putExtra("roomId", roomId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * 打开小组列表界面
     */
    private fun openGroupListActivity() {
        try {
            val intent = Intent(context, com.jxdx.classroom.group.TeacherViewActivity::class.java).apply {
                putExtra("subjectId", 1) // 默认值
                putExtra("teacherId", 6) // 默认值
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "成功启动TeacherViewActivity")
        } catch (e: Exception) {
            Log.e(TAG, "启动TeacherViewActivity失败", e)
            Toast.makeText(context, "启动小组列表界面失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 打开数学工具界面
     */
    private fun openMathToolActivity() {
        try {
            val intent = Intent(context, com.jxdx.classroom.activity.GeoGebraActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "成功启动GeoGebraActivity")
        } catch (e: Exception) {
            Log.e(TAG, "启动GeoGebraActivity失败", e)
            Toast.makeText(context, "启动数学工具界面失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 打开答题界面
     */
    private fun openQuizActivity() {
        // TODO: 实现答题功能
        Log.d(TAG, "打开答题界面，房间ID: $roomId")
    }
    
    /**
     * 打开发布资源界面
     */
    private fun openAttendanceActivity() {
        Log.d(TAG, "点击资料菜单，准备跳转到资料选择界面")
        try {
            val intent = Intent(context, ResourceSelectionActivity::class.java).apply {
                putExtra("roomId", roomId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "成功启动ResourceSelectionActivity")
        } catch (e: Exception) {
            Log.e(TAG, "启动ResourceSelectionActivity失败", e)
            Toast.makeText(context, "启动资料选择界面失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 检查悬浮窗权限
     */
    private fun checkFloatingPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        Log.d(TAG, "悬浮窗权限检查结果: $hasPermission, SDK版本: ${Build.VERSION.SDK_INT}")
        return hasPermission
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
                
                // 如果移动距离超过阈值，开始拖拽（降低阈值，更容易拖拽）
                if (!isDragging && (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3)) {
                    isDragging = true
                    Log.d(TAG, "开始拖拽 - deltaX: $deltaX, deltaY: $deltaY")
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
                    
                    Log.d(TAG, "拖拽悬浮球到: x=$clampedX, y=$clampedY")
                    
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
                
                if (isDragging) {
                    // 拖拽结束，悬浮球停在当前位置
                    Log.d(TAG, "悬浮球停在当前位置: x=${layoutParams?.x}, y=${layoutParams?.y}")
                } else {
                    // 如果没有拖拽，则显示菜单
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
