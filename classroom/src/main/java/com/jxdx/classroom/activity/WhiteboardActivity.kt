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
    
    // ViewModel
    private lateinit var viewModel: WhiteboardViewModel

    override fun bindLayout(): ActivityWhiteboardBinding {
        return ActivityWhiteboardBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[WhiteboardViewModel::class.java]
        
        setupToolbar()
        setupColorPickers()
        setupStrokeWidthSeekBar()
        setupToolButtons()
    }

    override fun subscribeUi() {
        // 初始化白板设置
        view.whiteboardView.setPaintColor(currentColor)
        view.whiteboardView.setStrokeWidth(5f)
        
        // 监听上传结果
        viewModel.uploadLiveData.observe(this) { result ->
            result.onSuccess { imageUrls ->
                if (imageUrls != null && imageUrls.isNotEmpty()) {
                    val imageUrl = imageUrls[0] // 获取第一个图片URL
                    Log.d(TAG, "白板图片上传成功，URL: $imageUrl")
                    Toast.makeText(this, "白板图片上传成功", Toast.LENGTH_SHORT).show()
                    
                    // TODO: 后续通过WebSocket发送图片URL给老师
                    // 这里可以添加WebSocket发送逻辑
                    
                    // 上传成功后返回上一页
                    finish()
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
            
            // 显示上传提示
            Toast.makeText(this, "正在上传白板图片...", Toast.LENGTH_SHORT).show()
            
            // 将Bitmap保存为临时文件
            val filePath = saveBitmapToFile(bitmap)
            if (filePath != null) {
                // 调用图片上传API
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

    override fun onBackPressed() {
        // 检查是否有内容未保存
        android.app.AlertDialog.Builder(this)
            .setTitle("退出白板")
            .setMessage("确定要退出吗？未提交的内容将丢失。")
            .setPositiveButton("退出") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
