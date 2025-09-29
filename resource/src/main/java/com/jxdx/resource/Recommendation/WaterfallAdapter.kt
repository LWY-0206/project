package com.jxdx.resource.Recommendation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.FirstPage.RecommendationItem
import com.jxdx.resource.R

class WaterfallAdapter(
    private var itemList: List<RecommendationItem> = emptyList()
) : RecyclerView.Adapter<WaterfallAdapter.ViewHolder>() {

    var onItemClickListener: ((RecommendationItem) -> Unit)? = null
    var onFavoriteClickListener: ((RecommendationItem) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvAuthor: TextView = itemView.findViewById(R.id.tv_author)
        val tvStats: TextView = itemView.findViewById(R.id.tv_stats)
        val ivFavorite: ImageView = itemView.findViewById(R.id.iv_favorite)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(itemList[adapterPosition])
            }

            ivFavorite.setOnClickListener {
                onFavoriteClickListener?.invoke(itemList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_waterfall, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // 设置基本文本信息
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvAuthor.text = item.author
        holder.tvStats.text = item.getFormattedStats()

        // 设置类型标签
        holder.tvType.text = if (item.isVideo()) "视频" else "文章"

        // 处理视频时长显示
        if (item.isVideo() && !item.duration.isNullOrEmpty()) {
            holder.tvDuration.text = item.duration
            holder.tvDuration.visibility = View.VISIBLE
        } else {
            holder.tvDuration.visibility = View.GONE
        }

        // 加载图片
        Glide.with(holder.itemView.context)
            .load(item.coverUrl)
            .into(holder.ivCover)

        // 设置收藏状态
        updateFavoriteIcon(holder.ivFavorite, item.isFavorited)
    }

    override fun getItemCount(): Int = itemList.size

    private fun updateFavoriteIcon(imageView: ImageView, isFavorited: Boolean) {
        if (isFavorited) {
            imageView.setImageResource(R.drawable.ic_favorite_filled)
            imageView.setColorFilter(ContextCompat.getColor(imageView.context, R.color.red))
        } else {
            imageView.setImageResource(R.drawable.ic_favorite_border)
            imageView.setColorFilter(ContextCompat.getColor(imageView.context, R.color.gray_400))
        }
    }

    // 更新数据的方法
    fun updateData(newItems: List<RecommendationItem>) {
        itemList = newItems
        notifyDataSetChanged()
    }

    // 添加新数据（用于加载更多）
    fun addData(newItems: List<RecommendationItem>) {
        val currentSize = itemList.size
        itemList = itemList + newItems
        notifyItemRangeInserted(currentSize, newItems.size)
    }
}