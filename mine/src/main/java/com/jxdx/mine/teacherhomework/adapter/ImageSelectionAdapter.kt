package com.jxdx.mine.teacherhomework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.mine.R

class ImageSelectionAdapter(
    private val images: List<Uri>,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<ImageSelectionAdapter.ViewHolder>() {

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
        val imageUri = images[position]
        
        // 加载图片
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_error)
            .into(holder.imageView)
        
        // 删除按钮点击事件
        holder.btnRemove.setOnClickListener {
            onRemoveClick(imageUri)
        }
    }

    override fun getItemCount(): Int = images.size
}
