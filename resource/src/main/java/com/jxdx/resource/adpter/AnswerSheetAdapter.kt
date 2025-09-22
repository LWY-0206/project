package com.jxdx.resource.adpter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.resource.Questions.ErrorQuizItem
import com.jxdx.resource.R

class AnswerSheetAdapter(
    private val questions: List<ErrorQuizItem>,
    private val answerResults: List<Boolean>,
    private val userAnswers: Map<Int, String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<AnswerSheetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_question_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_answer_sheet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = (position + 1).toString()

        // 根据答题结果设置背景色
        if (userAnswers.containsKey(position)) {
            if (answerResults[position]) {
                holder.textView.setBackgroundResource(R.drawable.bg_answer_correct)
            } else {
                holder.textView.setBackgroundResource(R.drawable.bg_answer_incorrect)
            }
            holder.textView.setTextColor(Color.WHITE)
        } else {
            holder.textView.setBackgroundResource(R.drawable.bg_answer_default)
            holder.textView.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = questions.size
}
