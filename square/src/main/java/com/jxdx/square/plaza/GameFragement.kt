package com.jxdx.square.plaza

import android.content.Intent
import android.widget.TextView
import com.example.corekit.common.BaseFragment
import com.jxdx.square.databinding.FragmentGameBinding

import kotlin.random.Random

class GameFragment : BaseFragment<FragmentGameBinding>() {
    // 积极向上的鼓励语句列表
    private val encouragingQuotes = listOf(
        "前\n方\n必\n有\n曙\n光",
        "每\n一\n步\n都\n是\n进\n步",
        "坚\n持\n就\n是\n胜\n利",
        "信\n心\n是\n成\n功\n的\n开\n始",
        "勇\n敢\n面\n对\n困\n难",
        "努\n力\n必\n有\n回\n报",
        "梦\n想\n需\n要\n坚\n持",
        "困\n难\n是\n成\n长\n的\n机\n会",
        "相\n信\n自\n己\n你\n能\n行",
        "每\n天\n都\n是\n新\n的\n开\n始"
    )

    override fun bindLayout(): FragmentGameBinding = FragmentGameBinding.inflate(layoutInflater)

    override fun initView() {
        // 查找文本视图
        val textView: TextView? = find.artisticText
        if (textView != null) {
            // 随机选择一条鼓励语句
            val randomQuote = encouragingQuotes[Random.nextInt(encouragingQuotes.size)]
            textView.text = randomQuote
            
            // 设置文本动画效果，让文字优雅地显示出来
            textView.alpha = 0f
            textView.animate()
                .alpha(1f)
                .setDuration(1000)
                .start()
            

        }
    }

    override fun subscribeUi() {
        // 无需要订阅的UI元素
    }
}