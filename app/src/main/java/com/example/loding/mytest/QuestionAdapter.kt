package com.example.loding.mytest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.example.corekit.recyclerview.SingleTypeAdapter
import com.example.corekit.recyclerview.SingleViewHolder
import com.example.loding.R
import com.example.loding.databinding.ItemQuestionBinding
import com.example.loding.entity.Question

class QuestionAdapter : SingleTypeAdapter<Question>() {
    override fun createViewHolder(
        viewType: Int,
        inflater: LayoutInflater,
        parent: ViewGroup,
    ): SingleViewHolder<ViewBinding, Any>? {
        // 传入对应需要的ViewBinding进行解析，并进行类型转换，注意as？
        return DemoListViewHolder(
            ItemQuestionBinding.inflate(inflater, parent, false),
        )
            as? SingleViewHolder<ViewBinding, Any>
    }

    /**
     * ViewHolder，构造内的属性建议通过alt+enter自动生成
     * 泛型1：ViewBinding,即绑定的Holder的视图生命
     * 泛型2：数据类型，即list中的泛型
     * 适配器是根据xml文件自动去生成的，比如布局文件名为 item_demo_entrance.xml → 生成 ItemDemoEntranceBinding
     */
    class DemoListViewHolder(
        private val binding: ItemQuestionBinding,
    ) : SingleViewHolder<ItemQuestionBinding, Question>(binding) {
        override fun setHolder(entity: Question) {
            // 1. 设置题干内容（带题号，如"1、新时代是我们理解当前所处历史方位的关键词。（）"）
            binding.tvContent.text = entity.content

            // 2. 根据题型（showType）处理选项
            when (entity.showType) {
                // 3代表判断题（数据中chooses为空数组）
                3 -> handleJudgmentQuestion(binding, entity)
                // 其他类型视为选择题（根据chooses列表显示选项）
                else -> handleChoiceQuestion(binding, entity)
            }
        }

        /**
         * 处理判断题（显示"正确"、"错误"选项）
         */
        private fun handleJudgmentQuestion(
            binding: ItemQuestionBinding,
            question: Question,
        ) {
            // 隐藏A、B、C、D原有选项容器，使用判断题专用选项
            binding.llChooses.visibility = View.GONE

            // 假设你在布局中添加了判断题选项容器（id: ll_judgment）
            binding.llJudgment.visibility = View.VISIBLE

            // 正确选项（√）
            binding.llTrue.setOnClickListener {
                // 处理正确选项点击（如记录用户选择）
                setJudgmentSelected(binding, isTrue = true)
            }

            // 错误选项（×）
            binding.llFalse.setOnClickListener {
                // 处理错误选项点击
                setJudgmentSelected(binding, isTrue = false)
            }
        }

        /**
         * 处理选择题（显示A、B、C、D选项）
         */
        private fun handleChoiceQuestion(
            binding: ItemQuestionBinding,
            question: Question,
        ) {
            // 隐藏判断题选项，显示选择题选项
            binding.llJudgment.visibility = View.GONE
            binding.llChooses.visibility = View.VISIBLE

            // 选项列表（A、B、C、D对应布局中的控件）
            val options =
                listOf(
                    binding.llOptionA to binding.tvContentA,
                    binding.llOptionB to binding.tvContentB,
                    binding.llOptionC to binding.tvContentC,
                    binding.llOptionD to binding.tvContentD,
                )

            // 遍历选项，赋值并设置点击事件
            options.forEachIndexed { index, (optionLayout, contentTv) ->
                if (index < question.chooses.size) {
                    // 显示选项并设置内容
                    optionLayout.visibility = View.VISIBLE
                    contentTv.text = question.chooses[index]

                    // 选项点击事件（记录用户选择）
                    optionLayout.setOnClickListener {
                        setChoiceSelected(binding, selectedIndex = index)
                    }
                } else {
                    // 隐藏多余选项（如只有2个选项时，隐藏C、D）
                    optionLayout.visibility = View.GONE
                }
            }
        }

        /**
         * 设置判断题选中状态
         */
        private fun setJudgmentSelected(
            binding: ItemQuestionBinding,
            isTrue: Boolean,
        ) {
            // 重置所有选项状态
            binding.llTrue.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_normal)
            binding.llFalse.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_normal)

            // 设置选中状态（使用选中样式的背景）
            if (isTrue) {
                binding.llTrue.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_selected)
            } else {
                binding.llFalse.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_selected)
            }
        }

        /**
         * 设置选择题选中状态
         */
        private fun setChoiceSelected(
            binding: ItemQuestionBinding,
            selectedIndex: Int,
        ) {
            // 重置所有选项状态
            listOf(
                binding.llOptionA,
                binding.llOptionB,
                binding.llOptionC,
                binding.llOptionD,
            ).forEach {
                it.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_normal)
            }

            // 设置选中选项的状态
            val selectedLayout =
                when (selectedIndex) {
                    0 -> binding.llOptionA
                    1 -> binding.llOptionB
                    2 -> binding.llOptionC
                    3 -> binding.llOptionD
                    else -> return
                }
            selectedLayout.background = ContextCompat.getDrawable(binding.root.context, R.drawable.option_bg_selected)
        }
    }
}
