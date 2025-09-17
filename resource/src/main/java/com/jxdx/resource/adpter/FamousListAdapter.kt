package com.jxdx.resource.Famous

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.R

class FamousListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FAMOUS = 1
        private const val TYPE_LOADING = 2
        private const val TYPE_ERROR = 3
    }

    private var famousList: MutableList<FamousRecord> = mutableListOf()
    private var hasMore = true
    private var isLoading = false
    private var isError = false
    private var errorRetry: (() -> Unit)? = null

    private var onItemClickListener: ((FamousRecord) -> Unit)? = null
    private var onLoadMoreListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FAMOUS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_famous, parent, false)
                FamousViewHolder(view)
            }
            TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            TYPE_ERROR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_error, parent, false)
                ErrorViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_FAMOUS -> {
                val famous = famousList[position]
                (holder as FamousViewHolder).bind(famous)
                holder.itemView.setOnClickListener {
                    onItemClickListener?.invoke(famous)
                }
            }
            TYPE_LOADING -> {
                // 加载更多
                if (hasMore && !isLoading) {
                    isLoading = true
                    onLoadMoreListener?.invoke()
                }
            }
            TYPE_ERROR -> {
                (holder as ErrorViewHolder).bind(errorRetry)
            }
        }
    }

    override fun getItemCount(): Int {
        var count = famousList.size
        if (hasMore || isError) count += 1 // 添加底部项
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= famousList.size) {
            if (isError) TYPE_ERROR else TYPE_LOADING
        } else {
            TYPE_FAMOUS
        }
    }

    fun setData(newFamousList: List<FamousRecord>) {
        famousList.clear()
        famousList.addAll(newFamousList)
        notifyDataSetChanged()
    }

    fun addData(newFamousList: List<FamousRecord>) {
        val startPosition = famousList.size
        famousList.addAll(newFamousList)
        notifyItemRangeInserted(startPosition, newFamousList.size)
    }

    fun setHasMore(hasMore: Boolean) {
        this.hasMore = hasMore
        this.isLoading = false
        this.isError = false
        notifyDataSetChanged()
    }

    fun showErrorView(retry: (() -> Unit)? = null) {
        this.isError = true
        this.errorRetry = retry
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (FamousRecord) -> Unit) {
        this.onItemClickListener = listener
    }

    fun setOnLoadMoreListener(listener: () -> Unit) {
        this.onLoadMoreListener = listener
    }

    class FamousViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvProfession: TextView = itemView.findViewById(R.id.tv_profession)
        private val tvEra: TextView = itemView.findViewById(R.id.tv_era)

        fun bind(famous: FamousRecord) {
            tvName.text = famous.celebrityName
            tvProfession.text = famous.profession
            tvEra.text = famous.era

            // 使用Glide加载头像
            Glide.with(itemView.context)
                .load(famous.avatarUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(ivAvatar)
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnRetry: TextView = itemView.findViewById(R.id.btn_retry)

        fun bind(retry: (() -> Unit)?) {
            btnRetry.setOnClickListener {
                retry?.invoke()
            }
        }
    }
}