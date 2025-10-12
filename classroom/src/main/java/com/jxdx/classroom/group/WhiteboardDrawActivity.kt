package com.jxdx.classroom.group

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.classroom.R
import com.jxdx.classroom.databinding.ActivityWhiteboardDrawBinding

class WhiteboardDrawActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWhiteboardDrawBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhiteboardDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // 设置标题
        supportActionBar?.title = "白板绘画"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 清除画布
        binding.btnClear.setOnClickListener {
            binding.whiteboardDrawView.clearCanvas()
            Toast.makeText(this, "画布已清除", Toast.LENGTH_SHORT).show()
        }

        // 撤销
        binding.btnUndo.setOnClickListener {
            binding.whiteboardDrawView.undo()
        }

        // 重做
        binding.btnRedo.setOnClickListener {
            binding.whiteboardDrawView.redo()
        }

        // 提交
        binding.btnSubmit.setOnClickListener {
            submitDrawing()
        }

        // 颜色选择
        binding.btnColorBlack.setOnClickListener {
            binding.whiteboardDrawView.setPaintColor(Color.BLACK)
            updateColorSelection(Color.BLACK)
        }
        binding.btnColorRed.setOnClickListener {
            binding.whiteboardDrawView.setPaintColor(Color.RED)
            updateColorSelection(Color.RED)
        }
        binding.btnColorBlue.setOnClickListener {
            binding.whiteboardDrawView.setPaintColor(Color.BLUE)
            updateColorSelection(Color.BLUE)
        }
        binding.btnColorGreen.setOnClickListener {
            binding.whiteboardDrawView.setPaintColor(Color.GREEN)
            updateColorSelection(Color.GREEN)
        }

        // 画笔大小
        binding.btnPenThin.setOnClickListener {
            binding.whiteboardDrawView.setPaintWidth(5f)
            updatePenSelection(5f)
        }
        binding.btnPenMedium.setOnClickListener {
            binding.whiteboardDrawView.setPaintWidth(10f)
            updatePenSelection(10f)
        }
        binding.btnPenThick.setOnClickListener {
            binding.whiteboardDrawView.setPaintWidth(20f)
            updatePenSelection(20f)
        }
    }

    private fun updateColorSelection(color: Int) {
        // 重置所有颜色按钮的选中状态
        binding.btnColorBlack.setBackgroundResource(R.drawable.bg_color_btn)
        binding.btnColorRed.setBackgroundResource(R.drawable.bg_color_btn)
        binding.btnColorBlue.setBackgroundResource(R.drawable.bg_color_btn)
        binding.btnColorGreen.setBackgroundResource(R.drawable.bg_color_btn)

        // 设置当前选中颜色的按钮
        when (color) {
            Color.BLACK -> binding.btnColorBlack.setBackgroundResource(R.drawable.bg_color_btn_selected)
            Color.RED -> binding.btnColorRed.setBackgroundResource(R.drawable.bg_color_btn_selected)
            Color.BLUE -> binding.btnColorBlue.setBackgroundResource(R.drawable.bg_color_btn_selected)
            Color.GREEN -> binding.btnColorGreen.setBackgroundResource(R.drawable.bg_color_btn_selected)
        }
    }

    private fun updatePenSelection(width: Float) {
        // 重置所有画笔按钮的选中状态
        binding.btnPenThin.setBackgroundResource(R.drawable.bg_pen_btn)
        binding.btnPenMedium.setBackgroundResource(R.drawable.bg_pen_btn)
        binding.btnPenThick.setBackgroundResource(R.drawable.bg_pen_btn)

        // 设置当前选中画笔的按钮
        when (width) {
            5f -> binding.btnPenThin.setBackgroundResource(R.drawable.bg_pen_btn_selected)
            10f -> binding.btnPenMedium.setBackgroundResource(R.drawable.bg_pen_btn_selected)
            20f -> binding.btnPenThick.setBackgroundResource(R.drawable.bg_pen_btn_selected)
        }
    }

    private fun submitDrawing() {
        val bitmap = binding.whiteboardDrawView.getBitmap()
        if (bitmap != null) {
            Toast.makeText(this, "绘画已提交", Toast.LENGTH_SHORT).show()
            // 这里可以添加实际的提交逻辑
            finish()
        } else {
            Toast.makeText(this, "请先进行绘画", Toast.LENGTH_SHORT).show()
        }
    }
}

class WhiteboardDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val path = Path()
    private val paths = mutableListOf<DrawingPath>()
    private val undonePaths = mutableListOf<DrawingPath>()
    private var currentPath = DrawingPath()

    data class DrawingPath(
        val path: Path = Path(),
        val paint: Paint = Paint()
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制背景
        canvas.drawColor(Color.WHITE)
        
        // 绘制所有路径
        paths.forEach { drawingPath ->
            canvas.drawPath(drawingPath.path, drawingPath.paint)
        }
        
        // 绘制当前路径
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                currentPath = DrawingPath().apply {
                    path.moveTo(x, y)
                    paint.set(this@WhiteboardDrawView.paint)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                currentPath.path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(currentPath)
                undonePaths.clear()
            }
        }
        invalidate()
        return true
    }

    fun setPaintColor(color: Int) {
        paint.color = color
    }

    fun setPaintWidth(width: Float) {
        paint.strokeWidth = width
    }

    fun clearCanvas() {
        paths.clear()
        undonePaths.clear()
        path.reset()
        invalidate()
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    fun redo() {
        if (undonePaths.isNotEmpty()) {
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    fun getBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        paths.forEach { drawingPath ->
            canvas.drawPath(drawingPath.path, drawingPath.paint)
        }
        
        return bitmap
    }
}
