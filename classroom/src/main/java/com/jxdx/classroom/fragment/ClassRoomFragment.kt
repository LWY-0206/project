package com.jxdx.classroom.fragment

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.corekit.common.BaseFragment
import com.jxdx.classroom.activity.ClassActivity

import com.jxdx.classroom.databinding.ClassroomFragmentBinding
import com.luck.picture.lib.utils.ToastUtils.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class ClassRoomFragment : BaseFragment<ClassroomFragmentBinding>() {
    private val dailyTips = listOf(
        "你知道吗？大脑在早晨学习效率最高哦～",
        "多喝水有助于提高注意力！",
        "适当的休息可以让学习更高效",
        "每天坚持学习比一次性长时间学习更有效",
        "运动后的大脑学习能力会更强",
        "🌱 学习25分钟后休息5分钟，效率提升明显",
        "📚 交替学习不同科目，避免大脑疲劳",
        "💡 教别人是最好的学习方式，掌握度达90%",
        "🎵 轻音乐有助于专注，但带歌词的音乐会分心",
        "✍️ 手写笔记比打字记忆更深刻",
        "🌞 自然光下学习对眼睛更好，也能提升心情",
        "🍎 学习前吃些坚果水果，补充脑力能量",
        "🧠 睡眠充足是记忆巩固的关键时期",
        "🔁 定期复习比一次性死记硬背更有效",
        "🎯 设定明确目标，学习更有方向感",
        "🔄 费曼学习法：用简单语言解释复杂概念",
        "⏰ 番茄工作法：25分钟专注+5分钟休息",
        "📖 主动回忆：合上书本尝试复述内容",
        "🔗 联想记忆：将新知识与已有知识联系",
        "💤 每天7-8小时睡眠让大脑充分休息",
        "🏃 运动促进脑细胞生长，提高记忆力",
        "🥦 均衡饮食，Omega-3脂肪酸有益大脑",
        "🧘 冥想练习能提升注意力和抗干扰能力",
        "🌟 成长型思维：相信能力可以通过努力提升",
        "💪 微小进步也值得庆祝，保持学习动力",
        "🤝 小组学习互相督促，效果1+1>2",
        "🎉 完成目标后给自己小奖励，形成正反馈",
        "📱 合理使用学习APP，但别过度依赖",
        "🎧 降噪耳机创造安静学习环境",
        "📊 用思维导图整理知识结构更清晰",
        "⏳ 时间管理工具帮你高效规划学习"
    )
    private var tipJob: Job? = null
    override fun bindLayout(): ClassroomFragmentBinding = ClassroomFragmentBinding.inflate(layoutInflater)
    override fun initView() {
        // 启动自动切换
        startAutoSwitchTips()

        // 点击卡片时也可以手动切换
        find.tvDailyTip.setOnClickListener {
            switchToNextTip()
        }

        //今日小知识
        find.tvDailyTip.text= dailyTips[Random.nextInt(dailyTips.size)]
        //进去课堂直播按钮
        find.btnEnterClass.setOnClickListener {
            val intent = Intent(requireActivity(), ClassActivity::class.java)
            startActivity(intent)
        }
        // 签到按钮
        find.btnDailyCheckin.setOnClickListener {
            showToast(context,"签到成功！+10积分")
            //
        }

        // 课前小测按钮
        find.btnPreclassChallenge.setOnClickListener {
            showToast(context,"开始课前小测")
            //
        }

        // 参与投票点击事件
        find.btnVote.setOnClickListener { // 需要给LinearLayout添加id
            showToast(context,"点击参与投票")
            //
        }

        // 我的小组区域点击事件
        find.myGroup.setOnClickListener { // 需要给LinearLayout添加id
            showToast(context,"进入我的小组")
            //
        }

        // 进入显示所有课堂直播按钮
        find.btnEnterAllClass.setOnClickListener {
            val intent = Intent(requireActivity(), ClassActivity::class.java)
            startActivity(intent)
        }
    }
    override fun subscribeUi() {

    }


    //今日小知识的自动切换
    private fun startAutoSwitchTips() {
        tipJob = lifecycleScope.launch {
            while (isActive) {
                switchToNextTip()
                delay(2000) // 延迟2秒
            }
        }
    }
    private fun switchToNextTip() {
        if (dailyTips.isNotEmpty()) {
            val randomTip = dailyTips[Random.nextInt(dailyTips.size)]
            // 添加切换动画（淡入）
            find.tvDailyTip.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    find.tvDailyTip.text = randomTip
                    find.tvDailyTip.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                }
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 停止自动切换
        tipJob?.cancel()
    }
}