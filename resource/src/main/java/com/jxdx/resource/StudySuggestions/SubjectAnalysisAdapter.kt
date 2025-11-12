package com.jxdx.resource.StudySuggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.resource.R

class SubjectAnalysisAdapter(
    private var subjectList: List<SubjectAnalyses> = emptyList()
) : RecyclerView.Adapter<SubjectAnalysisAdapter.ViewHolder>() {

    private var onItemClickListener: ((SubjectAnalyses) -> Unit)? = null
    private val expandedItems = mutableSetOf<Int>()

    fun setOnItemClickListener(listener: (SubjectAnalyses) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
        val tvAverageScore: TextView = itemView.findViewById(R.id.tv_average_score)
        val tvAssignments: TextView = itemView.findViewById(R.id.tv_assignments)
        val tvRecommendedHours: TextView = itemView.findViewById(R.id.tv_recommended_hours)
        val tvSuggestion: TextView = itemView.findViewById(R.id.tv_suggestion)
        val tvExpand: TextView = itemView.findViewById(R.id.tv_expand)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(subjectList[position])
                }
            }

            tvExpand.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleExpand(position)
                }
            }
        }

        private fun toggleExpand(position: Int) {
            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
                tvExpand.text = "展开"
                tvSuggestion.maxLines = 3
            } else {
                expandedItems.add(position)
                tvExpand.text = "收起"
                tvSuggestion.maxLines = Int.MAX_VALUE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_analysis, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjectList[position]

        holder.tvSubjectName.text = subject.subject
        holder.tvAverageScore.text = String.format("%.1f", subject.averageScore)
        holder.tvAssignments.text = "作业数: ${subject.totalAssignments}"
        holder.tvRecommendedHours.text = "建议: ${subject.recommendedHours}小时/周"
        holder.tvSuggestion.text = subject.suggestion

        // 设置展开状态
        if (expandedItems.contains(position)) {
            holder.tvExpand.text = "收起"
            holder.tvSuggestion.maxLines = Int.MAX_VALUE
        } else {
            holder.tvExpand.text = "展开"
            holder.tvSuggestion.maxLines = 3
        }

        // 根据分数设置颜色
        setScoreColor(holder.tvAverageScore, subject.averageScore)
    }

    private fun setScoreColor(textView: TextView, score: Double) {
        val colorRes = when {
            score >= 90 -> R.color.green_500
            score >= 80 -> R.color.blue_500
            score >= 60 -> R.color.orange_500
            else -> R.color.red_500
        }
        textView.setTextColor(textView.context.getColor(colorRes))
    }

    override fun getItemCount(): Int = subjectList.size

    fun setData(newList: List<SubjectAnalyses>) {
        subjectList = newList
        expandedItems.clear()
        notifyDataSetChanged()
    }
}