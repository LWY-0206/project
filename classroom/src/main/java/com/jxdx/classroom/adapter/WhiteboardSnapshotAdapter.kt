package com.jxdx.classroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jxdx.classroom.R
import com.jxdx.classroom.model.WhiteboardSnapshot
import java.text.SimpleDateFormat
import java.util.*

/**
 * 白板快照适配器
 */
class WhiteboardSnapshotAdapter(
    private var snapshots: List<WhiteboardSnapshot> = emptyList(),
    private val onItemClick: (WhiteboardSnapshot) -> Unit = {}
) : RecyclerView.Adapter<WhiteboardSnapshotAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSnapshot: ImageView = itemView.findViewById(R.id.iv_snapshot)
        val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val ivViewed: ImageView = itemView.findViewById(R.id.iv_viewed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_whiteboard_snapshot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val snapshot = snapshots[position]
        
        // 设置学生姓名
        holder.tvStudentName.text = snapshot.studentName
        
        // 设置时间戳
        val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = dateFormat.format(Date(snapshot.timestamp))
        
        // 设置查看状态
        holder.ivViewed.visibility = if (snapshot.isViewed) View.GONE else View.VISIBLE
        
        // 加载图片
        Glide.with(holder.itemView.context)
            .load(snapshot.imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_error)
            .into(holder.ivSnapshot)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(snapshot)
        }
    }

    override fun getItemCount(): Int = snapshots.size

    fun updateSnapshots(newSnapshots: List<WhiteboardSnapshot>) {
        snapshots = newSnapshots
        notifyDataSetChanged()
    }
}
