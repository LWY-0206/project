package com.jxdx.mine.teacherhomework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.mine.R

// 图片项数据类
data class ImageItem(
    val uri: Uri? = null,
    val url: String? = null,
    val isExisting: Boolean = false // 标识是否为现有图片
)

class MixedImageAdapter(
    private val images: MutableList<ImageItem>,
    private val onRemoveClick: (ImageItem) -> Unit
) : RecyclerView.Adapter<MixedImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        val btnRemove: ImageView = itemView.findViewById(R.id.btn_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageItem = images[position]
        
        // 加载图片
        when {
            imageItem.uri != null -> {
                // 本地Uri图片
                Glide.with(holder.itemView.context)
                    .load(imageItem.uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(holder.imageView)
            }
            imageItem.url != null -> {
                // 网络URL图片
                Glide.with(holder.itemView.context)
                    .load(imageItem.url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(holder.imageView)
            }
        }
        
        // 删除按钮点击事件
        holder.btnRemove.setOnClickListener {
            onRemoveClick(imageItem)
        }
    }

    override fun getItemCount(): Int = images.size
    
    // 添加新图片
    fun addImage(imageItem: ImageItem) {
        images.add(imageItem)
        notifyItemInserted(images.size - 1)
    }
    
    // 移除图片
    fun removeImage(imageItem: ImageItem) {
        val index = images.indexOf(imageItem)
        if (index != -1) {
            images.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
    // 获取所有图片
    fun getAllImages(): List<ImageItem> = images.toList()
}
