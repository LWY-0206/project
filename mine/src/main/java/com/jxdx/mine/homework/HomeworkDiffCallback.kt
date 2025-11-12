package com.jxdx.mine.homework

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
                oldItem.homeworkId == newItem.homeworkId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is SubjectGroup && newItem is SubjectGroup -> {
                oldItem.subjectName == newItem.subjectName &&
                oldItem.isExpanded == newItem.isExpanded &&
                oldItem.homeworkList.size == newItem.homeworkList.size &&
                oldItem.homeworkList.zip(newItem.homeworkList).all { (old, new) ->
                    old.homeworkId == new.homeworkId &&
                    old.homeworkName == new.homeworkName &&
                    old.completeAndCorrect == new.completeAndCorrect
                }
            }
            oldItem is Homework && newItem is Homework -> {
                oldItem.homeworkId == newItem.homeworkId &&
                oldItem.homeworkName == newItem.homeworkName &&
                oldItem.completeAndCorrect == newItem.completeAndCorrect &&
                oldItem.deadTime == newItem.deadTime &&
                oldItem.sendTime == newItem.sendTime
            }
            else -> false
        }
    }
}