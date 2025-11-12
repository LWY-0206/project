package com.jxdx.resource.Schools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.resource.R

class SchoolListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SCHOOL = 1
        private const val TYPE_LOADING = 2
        private const val TYPE_ERROR = 3
    }

    private var schools: MutableList<SchoolRecord> = mutableListOf()
    private var hasMore = true
    private var isLoading = false
    private var isError = false
    private var errorRetry: (() -> Unit)? = null

    private var onItemClickListener: ((SchoolRecord) -> Unit)? = null
    private var onLoadMoreListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SCHOOL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_school, parent, false)
                SchoolViewHolder(view)
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
            TYPE_SCHOOL -> {
                val school = schools[position]
                (holder as SchoolViewHolder).bind(school)
                holder.itemView.setOnClickListener {
                    onItemClickListener?.invoke(school)
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
        var count = schools.size
        if (hasMore || isError) count += 1 // 添加底部项
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= schools.size) {
            if (isError) TYPE_ERROR else TYPE_LOADING
        } else {
            TYPE_SCHOOL
        }
    }

    fun setData(newSchools: List<SchoolRecord>) {
        schools.clear()
        schools.addAll(newSchools)
        notifyDataSetChanged()
    }

    fun addData(newSchools: List<SchoolRecord>) {
        val startPosition = schools.size
        schools.addAll(newSchools)
        notifyItemRangeInserted(startPosition, newSchools.size)
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

    fun setOnItemClickListener(listener: (SchoolRecord) -> Unit) {
        this.onItemClickListener = listener
    }

    fun setOnLoadMoreListener(listener: () -> Unit) {
        this.onLoadMoreListener = listener
    }

    class SchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivEmblem: ImageView = itemView.findViewById(R.id.iv_emblem)
        private val tvSchoolName: TextView = itemView.findViewById(R.id.tv_school_name)

        fun bind(school: SchoolRecord) {
            tvSchoolName.text = school.schoolName

            // 使用Glide加载校徽
            Glide.with(itemView.context)
                .load(school.emblemUrl)
                .into(ivEmblem)
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