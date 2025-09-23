package com.jxdx.resource.News

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.News.NewsItem
import com.jxdx.resource.R

class NewsAdapter(private var items: List<NewsItem> = emptyList()) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    var onItemClickListener: ((NewsItem) -> Unit)? = null

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ImageView = itemView.findViewById(R.id.iv_news_cover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_news_title)
        private val tvSummary: TextView = itemView.findViewById(R.id.tv_news_summary)
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
            Log.d("bind","title=${newsItem.title}")
            tvSummary.text = newsItem.summary
            Log.d("bind","summary=${newsItem.summary}")
            tvSource.text = newsItem.source
            Log.d("bind","source=${newsItem.source}")
            tvTime.text = newsItem.getFormattedTime()
            tvViews.text = newsItem.getFormattedViews()

            // 加载图片
            Glide.with(itemView.context)
                .load(newsItem.coverUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivCover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        Log.d("使用viewHolder","初始化")
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position])
        Log.d("使用BindViewHolder","绑定")
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
        Log.d("更新数据","success")
    }

    fun addData(newItems: List<NewsItem>) {
        val startPosition = items.size
        items = items + newItems
        notifyItemRangeInserted(startPosition, newItems.size)
        Log.d("添加数据","success")
    }
}