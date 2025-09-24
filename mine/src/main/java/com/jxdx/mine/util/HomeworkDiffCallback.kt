package com.jxdx.mine.util

import androidx.recyclerview.widget.DiffUtil
import com.jxdx.mine.Homework
import com.jxdx.mine.SubjectGroup

class HomeworkDiffCallback(
    private val oldList: List<Any>,
    private val newList: List<Any>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is SubjectGroup && newItem is SubjectGroup ->
                oldItem.subjectName == newItem.subjectName
            oldItem is Homework && newItem is Homework ->
                oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}