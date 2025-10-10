package com.jxdx.classroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.classroom.R
import com.jxdx.classroom.entity.TeachingTool

/**
 * 工具选择适配器
 */
class ToolSelectionAdapter(
    private val onSelectionChanged: (TeachingTool) -> Unit
) : RecyclerView.Adapter<ToolSelectionAdapter.ToolViewHolder>() {

    private var tools = mutableListOf<TeachingTool>()

    fun updateTools(newTools: List<TeachingTool>) {
        tools.clear()
        tools.addAll(newTools)
        notifyDataSetChanged()
    }

    fun getSelectedTools(): List<TeachingTool> {
        return tools.filter { it.isSelected }
    }

    fun isAllSelected(): Boolean {
        return tools.isNotEmpty() && tools.all { it.isSelected }
    }

    fun selectAll() {
        tools.forEach { tool ->
            if (tool.isAvailable) {
                val index = tools.indexOf(tool)
                tools[index] = tool.copy(isSelected = true)
                notifyItemChanged(index)
            }
        }
    }

    fun clearSelection() {
        tools.forEachIndexed { index, tool ->
            tools[index] = tool.copy(isSelected = false)
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tool_selection, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        holder.bind(tool)
    }

    override fun getItemCount(): Int = tools.size

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_tool_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_tool_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_tool_description)
        private val cbSelected: CheckBox = itemView.findViewById(R.id.cb_tool_selected)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_selection_status)

        fun bind(tool: TeachingTool) {
            ivIcon.setImageResource(tool.iconResId)
            tvName.text = tool.name
            tvDescription.text = tool.description
            cbSelected.isChecked = tool.isSelected
            
            // 设置选择状态文字
            tvStatus.text = if (tool.isSelected) "已选择" else "未选择"
            tvStatus.setTextColor(
                if (tool.isSelected) 
                    itemView.context.getColor(android.R.color.holo_green_dark)
                else 
                    itemView.context.getColor(android.R.color.darker_gray)
            )
            
            // 设置可用性
            val alpha = if (tool.isAvailable) 1.0f else 0.5f
            itemView.alpha = alpha
            cbSelected.isEnabled = tool.isAvailable
            
            // 只设置整个卡片的点击事件，避免重复触发
            itemView.setOnClickListener {
                if (tool.isAvailable) {
                    try {
                        val index = tools.indexOf(tool)
                        if (index >= 0 && index < tools.size) {
                            val newTool = tool.copy(isSelected = !tool.isSelected)
                            tools[index] = newTool
                            notifyItemChanged(index)
                            onSelectionChanged(newTool)
                            android.util.Log.d("ToolSelectionAdapter", "卡片点击: ${tool.name} -> ${newTool.isSelected}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ToolSelectionAdapter", "点击事件处理失败", e)
                    }
                }
            }
        }
    }
}
