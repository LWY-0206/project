package com.jxdx.mine.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.Courseware
import com.jxdx.mine.R

class CoursewareAdapter(
    private val onPreviewClick: (Courseware) -> Unit,
    private val onDownloadClick: (Courseware) -> Unit
) : RecyclerView.Adapter<CoursewareAdapter.CoursewareViewHolder>() {

    private val coursewares = mutableListOf<Courseware>()

    fun submitList(list: List<Courseware>) {
        coursewares.clear()
        coursewares.addAll(list)
        notifyDataSetChanged()
    }

    inner class CoursewareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUploadTime: TextView = itemView.findViewById(R.id.tv_upload_time)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_courseware_title)
        private val btnPreview: Button = itemView.findViewById(R.id.btn_preview)
        private val btnDownload: Button = itemView.findViewById(R.id.btn_download)

        fun bind(courseware: Courseware) {
            tvUploadTime.text = "上传时间：${courseware.uploadTime}"
            tvTitle.text = courseware.title
            btnPreview.setOnClickListener { onPreviewClick(courseware) }
            btnDownload.setOnClickListener { onDownloadClick(courseware) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursewareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_courseware, parent, false)
        return CoursewareViewHolder(view)
    }

    override fun getItemCount() = coursewares.size
    override fun onBindViewHolder(holder: CoursewareViewHolder, position: Int) {
        holder.bind(coursewares[position])
    }
}
