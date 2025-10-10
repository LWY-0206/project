package com.jxdx.mine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.Homework
import com.jxdx.mine.SubjectGroup
import com.jxdx.mine.util.HomeworkDiffCallback

class HomeworkAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GROUP = 0
        private const val TYPE_ITEM = 1
    }

    private var groupList: MutableList<SubjectGroup> = mutableListOf()
    private var flatList: MutableList<Any> = mutableListOf()
    
    // 点击事件回调接口
    private var itemClickListener: ((Homework) -> Unit)? = null
    
    // 设置点击事件监听器
    fun setOnItemClickListener(listener: (Homework) -> Unit) {
        this.itemClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (flatList[position]) {
            is SubjectGroup -> TYPE_GROUP
            is Homework -> TYPE_ITEM
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GROUP -> {
                val view = inflater.inflate(R.layout.item_subject_group, parent, false)
                GroupViewHolder(view)
            }
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_homework, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupViewHolder -> {
                val group = flatList[position] as SubjectGroup
                holder.bind(group)
            }
            is ItemViewHolder -> {
                val homework = flatList[position] as Homework
                holder.bind(homework)
            }
        }
    }

    override fun getItemCount(): Int {
        return flatList.size
    }

    /**
     * 设置分组数据
     */
    fun setData(groups: List<SubjectGroup>) {
        groupList.clear()
        groupList.addAll(groups)

        val oldFlatList = flatList.toList()
        updateFlatList()

        val diffResult = DiffUtil.calculateDiff(HomeworkDiffCallback(oldFlatList, flatList))
        diffResult.dispatchUpdatesTo(this)
    }
    /**
     * 更新扁平化列表
     */
    private fun updateFlatList() {
        flatList.clear()
        groupList.forEach { group ->
            flatList.add(group)
            if (group.isExpanded) {
                flatList.addAll(group.homeworkList)
            }
        }
    }


    /**
     * 查找分组在 flatList 中的实际位置
     */
    private fun findGroupPosition(group: SubjectGroup): Int {
        return flatList.indexOfFirst { item ->
            item is SubjectGroup && item.subjectName == group.subjectName
        }
    }

    /**
     * 切换分组展开状态
     */
    private fun toggleGroupExpansion(group: SubjectGroup) {
        val groupPosition = findGroupPosition(group)
        if (groupPosition == -1) return

        group.isExpanded = !group.isExpanded
        val itemCount = group.homeworkList.size

        if (group.isExpanded) {
            // 展开：在分组后插入作业项
            flatList.addAll(groupPosition + 1, group.homeworkList)
            notifyItemRangeInserted(groupPosition + 1, itemCount)
        } else {
            // 收起：删除分组后的作业项
            flatList.subList(groupPosition + 1, groupPosition + 1 + itemCount).clear()
            notifyItemRangeRemoved(groupPosition + 1, itemCount)
        }

        // 更新分组项的箭头方向
        notifyItemChanged(groupPosition)
    }


    // 分组ViewHolder
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
        private val tvHomeworkCount: TextView = itemView.findViewById(R.id.tv_homework_count)
        private val ivExpand: ImageView = itemView.findViewById(R.id.iv_expand)

        fun bind(group: SubjectGroup) {
            tvSubjectName.text = group.subjectName
            tvHomeworkCount.text = "${group.homeworkList.size}份"
            ivExpand.rotation = if (group.isExpanded) 90f else 180f

            // 点击科目头部展开/折叠
            itemView.setOnClickListener {
                toggleGroupExpansion(group)
            }

            // 单独给箭头图标添加点击事件（可选）
            ivExpand.setOnClickListener {
                toggleGroupExpansion(group)
            }
        }
    }

    // 作业项ViewHolder
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHomeworkTitle: TextView = itemView.findViewById(R.id.tv_homework_title)
        private val tvDeadTime: TextView = itemView.findViewById(R.id.tv_dead_time)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvSenTime: TextView = itemView.findViewById(R.id.tv_send_time)
        private val context: Context = itemView.context

        fun bind(homework: Homework) {
            tvHomeworkTitle.text = homework.homeworkName
            tvSenTime.text="发布日期：${homework.sendTime}"
            tvDeadTime.text = "截止日期：${homework.deadTime}"

            when (homework.completeAndCorrect) {
                1-> {
                    tvStatus.text = "未提交"
                    tvStatus.setBackgroundResource(R.drawable.status_tag)
                }
                2 -> {
                    tvStatus.text = "待批改"
                    tvStatus.setBackgroundResource(R.drawable.status_tag_pending)
                }
                3 -> {
                    tvStatus.text = "已完成"
                    tvStatus.setBackgroundResource(R.drawable.status_tag_completed)
                }
            }

            // 作业项点击
            itemView.setOnClickListener {
                // 调用外部设置的点击事件监听器
                itemClickListener?.invoke(homework)
            }
        }
    }
}