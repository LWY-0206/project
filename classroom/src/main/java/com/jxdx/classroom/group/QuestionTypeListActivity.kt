package com.jxdx.classroom.group

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jxdx.classroom.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 题型列表Activity
 * 用于显示老师提前设置的题型列表和添加新题型
 */
class QuestionTypeListActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var teacherId = 1
    private var subjectId = 1
    private var isSelectMode = false
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddQuestionType: Button
    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    
    private val questionTypes = mutableListOf<QuestionType>()
    private lateinit var adapter: QuestionTypeAdapter
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_type_list)
        
        // 初始化控件
        recyclerView = findViewById(R.id.rvQuestionTypes)
        btnAddQuestionType = findViewById(R.id.btnAddQuestionType)
        ivBack = findViewById(R.id.ivBack)
        tvTitle = findViewById(R.id.tvTitle)
        
        // 获取从上一个界面传递的参数
        teacherId = intent.getIntExtra("teacherId", 1)
        subjectId = intent.getIntExtra("subjectId", 1)
        isSelectMode = intent.getBooleanExtra("selectMode", false)
        
        // 设置标题和按钮可见性
        if (isSelectMode) {
            tvTitle.text = "选择题型"
            btnAddQuestionType.visibility = View.GONE
        } else {
            tvTitle.text = "题型管理"
            btnAddQuestionType.visibility = View.VISIBLE
        }
        
        // 设置RecyclerView
        setupRecyclerView()
        
        // 加载题型列表
        loadQuestionTypes()
        
        // 设置点击事件
        setOnClickListener()
    }
    
    private fun setupRecyclerView() {
        adapter = QuestionTypeAdapter(questionTypes, isSelectMode) { questionType ->
            onQuestionTypeClick(questionType)
        }
        adapter.setEditClickListener { questionType ->
            onEditQuestionType(questionType)
        }
        adapter.setDeleteClickListener { questionType ->
            onDeleteQuestionType(questionType)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadQuestionTypes() {
        launch {
            try {
                // 显示加载提示
                Toast.makeText(this@QuestionTypeListActivity, "正在加载题型列表...", Toast.LENGTH_SHORT).show()
                
                // 从QuestionTypeManager获取题型列表
                val result = QuestionTypeManager.instance.getAllQuestionTypes(teacherId)
                if (result.isSuccess) {
                    questionTypes.clear()
                    questionTypes.addAll(result.getOrNull() ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    // 加载失败，显示模拟数据
                    showMockData()
                    Toast.makeText(this@QuestionTypeListActivity, "加载题型列表失败，显示模拟数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("QuestionTypeListActivity", "加载题型列表异常: ${e.message}")
                showMockData()
                Toast.makeText(this@QuestionTypeListActivity, "加载题型列表异常，显示模拟数据", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showMockData() {
        // 清除现有数据
        questionTypes.clear()
        
        // 添加模拟数据
        questionTypes.add(QuestionType(
            id = 1,
            title = "讨论题示例1",
            content = "请讨论人工智能对未来教育的影响",
            images = emptyList(),
            files = emptyList()
        ))
        questionTypes.add(QuestionType(
            id = 2,
            title = "案例分析题",
            content = "分析这个商业案例中的营销策略",
            images = listOf("https://example.com/image1.jpg"),
            files = listOf("https://example.com/file1.pdf")
        ))
        questionTypes.add(QuestionType(
            id = 3,
            title = "小组协作题",
            content = "小组合作完成一个项目计划书",
            images = emptyList(),
            files = emptyList()
        ))
        
        // 通知适配器数据已更改
        adapter.notifyDataSetChanged()
    }
    
    private fun setOnClickListener() {
        // 返回按钮点击事件
        ivBack.setOnClickListener {
            finish()
        }
        
        // 添加题型按钮点击事件
        btnAddQuestionType.setOnClickListener {
            showAddQuestionTypeDialog()
        }
    }
    
    /**
     * 显示添加题型对话框
     */
    private fun showAddQuestionTypeDialog(questionType: QuestionType? = null) {
        // 创建对话框
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_question_type, null)
        builder.setView(view)
        val dialog = builder.create()
        
        // 获取对话框中的控件
        val tvDialogTitle = view.findViewById<TextView>(R.id.tv_dialog_title)
        val etTitle = view.findViewById<EditText>(R.id.et_title)
        val etContent = view.findViewById<EditText>(R.id.et_content)
        val btnAddImage = view.findViewById<Button>(R.id.btn_add_image)
        val btnAddFile = view.findViewById<Button>(R.id.btn_add_file)
        val llSelectedAttachments = view.findViewById<LinearLayout>(R.id.ll_selected_attachments)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
        
        // 设置标题和初始值
        val isEditMode = questionType != null
        tvDialogTitle.text = if (isEditMode) "编辑题型" else "添加题型"
        
        if (isEditMode) {
            questionType?.let {
                etTitle.setText(it.title)
                etContent.setText(it.content)
                // 如果有附件，显示已选附件
                if (it.images.isNotEmpty() || it.files.isNotEmpty()) {
                    llSelectedAttachments.visibility = View.VISIBLE
                    // 这里可以添加图片和文件的显示逻辑
                }
            }
        }
        
        // 添加图片按钮点击事件
        btnAddImage.setOnClickListener {
            Toast.makeText(this, "添加图片功能暂未实现", Toast.LENGTH_SHORT).show()
        }
        
        // 添加文件按钮点击事件
        btnAddFile.setOnClickListener {
            Toast.makeText(this, "添加文件功能暂未实现", Toast.LENGTH_SHORT).show()
        }
        
        // 取消按钮点击事件
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // 确定按钮点击事件
        btnConfirm.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            
            if (title.isEmpty()) {
                Toast.makeText(this, "请输入题型标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入题型内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 创建或更新题型
            val newQuestionType = if (isEditMode) {
                questionType?.copy(title = title, content = content) ?: 
                    QuestionType(id = questionType?.id ?: 0, title = title, content = content)
            } else {
                QuestionType(
                    id = (System.currentTimeMillis() % 10000).toInt(), // 临时ID
                    title = title,
                    content = content,
                    images = emptyList(),
                    files = emptyList()
                )
            }
            
            // 保存题型
            saveQuestionType(newQuestionType, isEditMode)
            dialog.dismiss()
        }
        
        // 显示对话框
        dialog.show()
    }
    
    /**
     * 保存题型
     */
    private fun saveQuestionType(questionType: QuestionType, isEditMode: Boolean) {
        launch {
            try {
                // 显示保存提示
                Toast.makeText(this@QuestionTypeListActivity, "正在保存题型...", Toast.LENGTH_SHORT).show()
                
                val result = if (isEditMode) {
                    QuestionTypeManager.instance.updateQuestionType(questionType)
                } else {
                    QuestionTypeManager.instance.addQuestionType(questionType)
                }
                
                if (result.isSuccess) {
                    // 保存成功，刷新列表
                    loadQuestionTypes()
                    Toast.makeText(
                        this@QuestionTypeListActivity, 
                        if (isEditMode) "题型更新成功" else "题型添加成功", 
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // 保存失败
                    Toast.makeText(
                        this@QuestionTypeListActivity, 
                        if (isEditMode) "题型更新失败" else "题型添加失败", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("QuestionTypeListActivity", "保存题型异常: ${e.message}")
                Toast.makeText(
                    this@QuestionTypeListActivity, 
                    if (isEditMode) "题型更新异常" else "题型添加异常", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * 处理题型点击事件
     */
    private fun onQuestionTypeClick(questionType: QuestionType) {
        if (isSelectMode) {
            // 选择模式下，返回选择的题型（直接使用序列化对象）
            val resultIntent = Intent()
            resultIntent.putExtra("selectedQuestionType", questionType)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    
    /**
     * 处理编辑题型点击事件
     */
    private fun onEditQuestionType(questionType: QuestionType) {
        if (!isSelectMode) {
            showAddQuestionTypeDialog(questionType)
        }
    }
    
    /**
     * 处理删除题型点击事件
     */
    private fun onDeleteQuestionType(questionType: QuestionType) {
        if (!isSelectMode) {
            // 显示确认对话框
            AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除题型'${questionType.title}'吗？")
                .setPositiveButton("确定") { dialog, which ->
                    // 确认删除
                    deleteQuestionType(questionType.id)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
    
    /**
     * 删除题型
     */
    private fun deleteQuestionType(id: Int) {
        launch {
            try {
                // 显示删除提示
                Toast.makeText(this@QuestionTypeListActivity, "正在删除题型...", Toast.LENGTH_SHORT).show()
                
                val result = QuestionTypeManager.instance.deleteQuestionType(id)
                
                if (result.isSuccess) {
                    // 删除成功，刷新列表
                    loadQuestionTypes()
                    Toast.makeText(this@QuestionTypeListActivity, "题型删除成功", Toast.LENGTH_SHORT).show()
                } else {
                    // 删除失败
                    Toast.makeText(this@QuestionTypeListActivity, "题型删除失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("QuestionTypeListActivity", "删除题型异常: ${e.message}")
                Toast.makeText(this@QuestionTypeListActivity, "题型删除异常", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}

/**
 * 题型列表适配器
 * 用于显示题型列表数据
 */
class QuestionTypeAdapter(
    private val questionTypes: List<QuestionType>,
    private val isSelectMode: Boolean,
    private val onItemClick: (QuestionType) -> Unit
) : RecyclerView.Adapter<QuestionTypeAdapter.ViewHolder>() {
    
    // 编辑和删除的点击监听器
    private var onEditClick: ((QuestionType) -> Unit)? = null
    private var onDeleteClick: ((QuestionType) -> Unit)? = null
    
    // 设置编辑点击监听器
    fun setEditClickListener(listener: (QuestionType) -> Unit) {
        this.onEditClick = listener
    }
    
    // 设置删除点击监听器
    fun setDeleteClickListener(listener: (QuestionType) -> Unit) {
        this.onDeleteClick = listener
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestionTitle: TextView = itemView.findViewById(R.id.tv_question_title)
        val tvQuestionContent: TextView = itemView.findViewById(R.id.tv_question_content)
        val llAttachments: LinearLayout = itemView.findViewById(R.id.ll_attachments)
        val llActions: LinearLayout = itemView.findViewById(R.id.ll_actions)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_type, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionType = questionTypes[position]
        
        // 设置题型标题和内容
        holder.tvQuestionTitle.text = questionType.title
        holder.tvQuestionContent.text = questionType.content
        
        // 设置附件显示
        if (questionType.images.isNotEmpty() || questionType.files.isNotEmpty()) {
            holder.llAttachments.visibility = View.VISIBLE
            // 这里可以动态添加图片和文件图标
            holder.llAttachments.removeAllViews()
            
            // 添加图片图标
            questionType.images.take(2).forEach {
                addAttachmentIcon(holder.llAttachments, R.drawable.ic_image, "图片")
            }
            
            // 添加文件图标
            questionType.files.take(2).forEach {
                addAttachmentIcon(holder.llAttachments, R.drawable.ic_file, "文件")
            }
            
            // 如果附件数量超出显示，添加省略号
            val totalAttachments = questionType.images.size + questionType.files.size
            if (totalAttachments > 4) {
                addMoreAttachmentIcon(holder.llAttachments, totalAttachments - 4)
            }
        } else {
            holder.llAttachments.visibility = View.GONE
        }
        
        // 设置操作按钮可见性
        if (isSelectMode) {
            holder.llActions.visibility = View.GONE
        } else {
            holder.llActions.visibility = View.VISIBLE
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(questionType)
        }
        
        // 设置编辑按钮点击事件
        holder.btnEdit.setOnClickListener {
            onEditClick?.invoke(questionType)
        }
        
        // 设置删除按钮点击事件
        holder.btnDelete.setOnClickListener {
            onDeleteClick?.invoke(questionType)
        }
    }
    
    override fun getItemCount(): Int {
        return questionTypes.size
    }
    
    /**
     * 添加附件图标
     */
    private fun addAttachmentIcon(container: LinearLayout, iconRes: Int, label: String) {
        val context = container.context
        val iconView = ImageView(context)
        val layoutParams = LinearLayout.LayoutParams(40, 40)
        layoutParams.setMargins(0, 0, 8, 0)
        iconView.layoutParams = layoutParams
        iconView.setImageResource(iconRes)
        iconView.contentDescription = label
        container.addView(iconView)
    }
    
    /**
     * 添加更多附件图标
     */
    private fun addMoreAttachmentIcon(container: LinearLayout, count: Int) {
        val context = container.context
        val textView = TextView(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 40)
        layoutParams.setMargins(0, 0, 8, 0)
        textView.layoutParams = layoutParams
        textView.text = "+\$count"
        textView.gravity = android.view.Gravity.CENTER
        textView.setTextColor(context.resources.getColor(R.color.text_secondary))
        textView.textSize = 12f
        container.addView(textView)
    }
}