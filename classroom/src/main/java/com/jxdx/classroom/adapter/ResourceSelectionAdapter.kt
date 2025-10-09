package com.jxdx.classroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.classroom.R
import com.jxdx.classroom.entity.ResourceItem

/**
 * 资料选择适配器
 * 用于显示可选择的资料列表
 */
class ResourceSelectionAdapter(
    private val onItemClick: (ResourceItem, Boolean) -> Unit
) : RecyclerView.Adapter<ResourceSelectionAdapter.ViewHolder>() {
    
    private val resources = mutableListOf<ResourceItem>()
    private val selectedItems = mutableSetOf<ResourceItem>()
    
    /**
     * 更新数据
     */
    fun updateData(newResources: List<ResourceItem>) {
        resources.clear()
        resources.addAll(newResources)
        notifyDataSetChanged()
    }
    
    /**
     * 设置选中状态
     */
    fun setSelectedItems(selected: Set<ResourceItem>) {
        selectedItems.clear()
        selectedItems.addAll(selected)
        notifyDataSetChanged()
    }
    
    /**
     * 获取选中的资料
     */
    fun getSelectedItems(): List<ResourceItem> {
        return selectedItems.toList()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resource_selection, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = resources[position]
        val isSelected = selectedItems.contains(resource)
        
        holder.bind(resource, isSelected)
    }
    
    override fun getItemCount(): Int = resources.size
    
    /**
     * ViewHolder
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivResource: ImageView = itemView.findViewById(R.id.iv_resource_image)
        private val tvResourceName: TextView = itemView.findViewById(R.id.tv_resource_name)
        private val ivSelectionIndicator: ImageView = itemView.findViewById(R.id.iv_selection_indicator)
        private val cardImageContainer: androidx.cardview.widget.CardView = itemView.findViewById(R.id.card_image_container)
        
        fun bind(resource: ResourceItem, isSelected: Boolean) {
            // 设置资源名称
            tvResourceName.text = resource.name
            
            // 设置资源图片
            when (resource.type) {
                ResourceItem.Type.IMAGE -> {
                    // 对于图片类型，使用Glide加载网络图片
                    if (resource.url.isNotEmpty()) {
                        Glide.with(itemView.context)
                            .load(resource.url)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .into(ivResource)
                    } else {
                        ivResource.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
                ResourceItem.Type.DOCUMENT -> {
                    ivResource.setImageResource(R.drawable.ic_document)
                }
                ResourceItem.Type.VIDEO -> {
                    ivResource.setImageResource(R.drawable.ic_video_placeholder)
                }
                ResourceItem.Type.AUDIO -> {
                    ivResource.setImageResource(R.drawable.ic_audio)
                }
            }
            
            // 设置选中状态 - 只在选中时显示√记号
            ivSelectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            // 设置卡片选中状态
            cardImageContainer.isSelected = isSelected
            
            // 设置整个item的选中状态
            itemView.isSelected = isSelected
            
            // 设置点击事件
            itemView.setOnClickListener {
                val newSelected = !isSelected
                // 更新选中状态
                if (newSelected) {
                    selectedItems.add(resource)
                } else {
                    selectedItems.remove(resource)
                }
                // 通知Adapter更新UI
                notifyItemChanged(adapterPosition)
                // 回调给Activity
                onItemClick(resource, newSelected)
            }
        }
    }
}
