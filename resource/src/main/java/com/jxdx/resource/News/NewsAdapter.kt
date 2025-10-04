package com.jxdx.resource.News

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.R

class NewsAdapter(private var items: List<NewsItem> = emptyList()) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    var onItemClickListener: ((NewsItem) -> Unit)? = null

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ImageView = itemView.findViewById(R.id.iv_news_cover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        private val tvSource: TextView = itemView.findViewById(R.id.tv_news_source)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_news_time)
        private val tvViews: TextView = itemView.findViewById(R.id.tv_news_views)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(items[position])
                }
            }
        }

        fun bind(newsItem: NewsItem) {
            tvTitle.text = newsItem.title
            Log.d("NewsAdapter", "绑定标题: ${newsItem.title}")

            tvSource.text = newsItem.source
            Log.d("NewsAdapter", "绑定来源: ${newsItem.source}")

            tvTime.text = newsItem.getFormattedTime()
            tvViews.text = newsItem.getFormattedViews()

            // 加载图片
            Glide.with(itemView.context)
                .load(newsItem.coverUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivCover)

            Log.d("NewsAdapter", "加载图片: ${newsItem.coverUrl}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        Log.d("NewsAdapter", "创建ViewHolder")
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
        Log.d("NewsAdapter", "绑定ViewHolder位置: $position")
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
        Log.d("NewsAdapter", "更新数据，数量: ${newItems.size}")
    }

    fun addData(newItems: List<NewsItem>) {
        val startPosition = items.size
        items = items + newItems
        notifyItemRangeInserted(startPosition, newItems.size)
        Log.d("NewsAdapter", "添加数据，数量: ${newItems.size}")
    }
}