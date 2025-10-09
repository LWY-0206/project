package com.jxdx.mine.teacherhomework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.databinding.ItemHomeworkLibraryBinding

class HomeworkLibraryAdapter(
    private val homeworkList: List<HomeworkDetail>,
    private val onClick: (HomeworkDetail) -> Unit
) : RecyclerView.Adapter<HomeworkLibraryAdapter.HomeworkViewHolder>() {
    
    private var isDeleteMode = false
    private val selectedItems = mutableSetOf<String>()
    private var onSelectionChange: ((String, Boolean) -> Unit)? = null
    
    inner class HomeworkViewHolder(val binding: ItemHomeworkLibraryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkLibraryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        val homework = homeworkList[position]
        
        // 设置作业标题
        holder.binding.tvHomeworkTitle.text = homework.title
        
        // 设置状态
        holder.binding.tvStatus.text = "发布"
        
        // 设置截止时间
        if (homework.dueDate.isNotEmpty()) {
            holder.binding.tvDeadTime.text = "截止时间: ${homework.dueDate}"
        } else {
            holder.binding.tvDeadTime.text = ""
        }
        
        // 设置提交状态
        val submissionCount = homework.submissions.filter { it.content.isNotEmpty() }.size
        if (submissionCount == 0) {
            holder.binding.tvSubmissionStatus.text = "暂无提交"
        } else {
            holder.binding.tvSubmissionStatus.text = "已提交: ${submissionCount}人"
        }

        // 设置复选框的显示和状态
        if (isDeleteMode) {
            holder.binding.checkboxDelete.visibility = android.view.View.VISIBLE
            holder.binding.checkboxDelete.isChecked = selectedItems.contains(homework.id)
            
            // 复选框点击事件
            holder.binding.checkboxDelete.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(homework.id)
                } else {
                    selectedItems.remove(homework.id)
                }
                onSelectionChange?.invoke(homework.id, isChecked)
            }
            
            // 整个item点击事件（在删除模式下点击复选框）
            holder.itemView.setOnClickListener {
                holder.binding.checkboxDelete.isChecked = !holder.binding.checkboxDelete.isChecked
            }
        } else {
            holder.binding.checkboxDelete.visibility = android.view.View.GONE
            holder.itemView.setOnClickListener { onClick(homework) }
        }
    }

    override fun getItemCount(): Int = homeworkList.size
    
    fun setDeleteMode(deleteMode: Boolean) {
        isDeleteMode = deleteMode
        if (!deleteMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun setOnSelectionChangeListener(listener: (String, Boolean) -> Unit) {
        onSelectionChange = listener
    }
}
