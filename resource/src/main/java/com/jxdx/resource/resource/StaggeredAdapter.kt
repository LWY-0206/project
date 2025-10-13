package com.jxdx.resource.resource

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.R

class StaggeredAdapter(
    private val context: Context,
    private val dataList: MutableList<StaggeredItem>?
) : RecyclerView.Adapter<StaggeredAdapter.StaggeredViewHolder>() { // 修复点1：泛型参数不再使用 ?，因为类已非空

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaggeredViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staggered, parent, false)
        return StaggeredViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StaggeredViewHolder, position: Int) {
        val item = dataList?.getOrNull(position) ?: return // 修复点2：避免!!，增强安全性
        Glide.with(context)
            .load(item.imageUrl)
            .into(holder.imageView)
        holder.titleTextView.text = item.title
        holder.descTextView.text = item.desc
    }
    fun updateData(newDataList: MutableList<StaggeredItem>) {
        dataList?.clear()
        dataList?.addAll(newDataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList?.size ?: 0

    // 修复点3：移除 internal 修饰符，改为默认 public 可见性
    class StaggeredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_item_image)
        val titleTextView: TextView = itemView.findViewById(R.id.tv_item_title)
        val descTextView: TextView = itemView.findViewById(R.id.tv_item_desc)
    }
}