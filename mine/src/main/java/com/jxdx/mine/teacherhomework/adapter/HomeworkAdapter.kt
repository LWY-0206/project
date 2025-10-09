package com.jxdx.mine.teacherhomework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.HomeworkDetail
import com.jxdx.mine.R
import com.jxdx.mine.databinding.ItemHomeworkBinding

class HomeworkAdapter(
    private val homeworkList: MutableList<HomeworkDetail>,
    private val onClick: (HomeworkDetail) -> Unit
) : RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {
    
    private var isDeleteMode = false
    private val selectedItems = mutableSetOf<String>()
    private var onSelectionChange: ((String, Boolean) -> Unit)? = null
    inner class HomeworkViewHolder(val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
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
        if (homework.isPublished) {
            holder.binding.tvSendTime.text = "✅ 已发布"
            if (homework.publishTime != null) {
                holder.binding.tvSendTime.text = "✅ 发布于: ${homework.publishTime}"
            }
            // 已发布的作业使用绿色文字
            holder.binding.tvSendTime.setTextColor(holder.itemView.context.getColor(R.color.green_dark))
        } else {
            holder.binding.tvSendTime.text = "📝 草稿"
            // 草稿使用灰色文字
            holder.binding.tvSendTime.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        }
        
        // 设置截止时间
        if (homework.dueDate.isNotEmpty()) {
            holder.binding.tvDeadTime.text = "截止时间: ${homework.dueDate}"
        } else {
            holder.binding.tvDeadTime.text = ""
        }
        
        // 设置提交状态
        val submissionCount = homework.submissions.filter { it.content.isNotEmpty() }.size
        val reviewedCount = homework.submissions.count { it.isReviewed }
        
        if (submissionCount == 0) {
            holder.binding.tvSubmissionStatus.text = "📝 暂无提交"
            holder.binding.tvSubmissionStatus.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        } else {
            if (reviewedCount == submissionCount) {
                // 全部已批改
                holder.binding.tvSubmissionStatus.text = "✅ 已批改: ${submissionCount}人"
                holder.binding.tvSubmissionStatus.setTextColor(holder.itemView.context.getColor(R.color.green_dark))
            } else {
                // 部分已批改
                holder.binding.tvSubmissionStatus.text = "📊 已提交: ${submissionCount}人 (已批改: ${reviewedCount}人)"
                holder.binding.tvSubmissionStatus.setTextColor(holder.itemView.context.getColor(R.color.primary_color))
            }
        }

        // 设置复选框的显示和状态
        if (isDeleteMode) {
            holder.binding.checkboxDelete.alpha = 1f
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
            holder.binding.checkboxDelete.alpha = 0f
            holder.binding.checkboxDelete.isChecked = false
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
    
    fun updateData(newData: List<HomeworkDetail>) {
        homeworkList.clear()
        homeworkList.addAll(newData)
        notifyDataSetChanged()
    }
}