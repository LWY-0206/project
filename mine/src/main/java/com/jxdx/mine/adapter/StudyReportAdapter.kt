package com.jxdx.mine.adapter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R

// 图表数据类
data class ChartData(
    val label: String,
    val value: Float
)

// 学习报告卡片类型枚举
enum class StudyReportCardType {
    OVERVIEW,      // 学习概览
    HOMEWORK,      // 作业完成
    ATTENDANCE,    // 出勤记录
    EXAM,          // 考试成绩
    PARTICIPATION, // 课堂参与
    SUMMARY        // 学习总结
}

// 学习报告卡片数据类
data class StudyReportCard(
    val type: StudyReportCardType,
    val title: String,
    val subtitle: String,
    val data: Map<String, String>,
    val chartData: List<ChartData>
)

class StudyReportAdapter : RecyclerView.Adapter<StudyReportAdapter.StudyReportViewHolder>() {
    
    private var cards = listOf<StudyReportCard>()
    
    fun submitList(newCards: List<StudyReportCard>) {
        cards = newCards
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_report_card_safe, parent, false)
        return StudyReportViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StudyReportViewHolder, position: Int) {
        holder.bind(cards[position])
    }
    
    override fun getItemCount(): Int = cards.size
    
    class StudyReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_card_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_card_subtitle)
        private val tvData1: TextView = itemView.findViewById(R.id.tv_data_1)
        private val tvData2: TextView = itemView.findViewById(R.id.tv_data_2)
        private val tvData3: TextView = itemView.findViewById(R.id.tv_data_3)
        private val tvChartInfo: TextView = itemView.findViewById(R.id.tv_chart_info)
        
        fun bind(card: StudyReportCard) {
            val titleEmoji = getTitleEmoji(card.type)
            tvTitle.text = "$titleEmoji ${card.title}"
            tvSubtitle.text = card.subtitle
            
            // 设置数据
            val dataEntries = card.data.entries.toList()
            if (dataEntries.isNotEmpty()) {
                val key1 = dataEntries[0].key
                val value1 = dataEntries[0].value
                val emoji1 = getDataEmoji(key1)
                tvData1.text = "$emoji1 $key1: $value1"
                tvData1.visibility = View.VISIBLE
            } else {
                tvData1.visibility = View.GONE
            }
            
            if (dataEntries.size > 1) {
                val key2 = dataEntries[1].key
                val value2 = dataEntries[1].value
                val emoji2 = getDataEmoji(key2)
                tvData2.text = "$emoji2 $key2: $value2"
                tvData2.visibility = View.VISIBLE
            } else {
                tvData2.visibility = View.GONE
            }
            
            if (dataEntries.size > 2) {
                val key3 = dataEntries[2].key
                val value3 = dataEntries[2].value
                val emoji3 = getDataEmoji(key3)
                tvData3.text = "$emoji3 $key3: $value3"
                tvData3.visibility = View.VISIBLE
            } else {
                tvData3.visibility = View.GONE
            }
            
            // 设置图表信息 - 用文本和emoji模拟图表效果
            val chartText = buildChartText(card.chartData)
            tvChartInfo.text = chartText
            
            // 根据卡片类型设置不同的背景色
            setCardBackground(card.type)
        }
        
        private fun setCardBackground(type: StudyReportCardType) {
            val backgroundRes = when (type) {
                StudyReportCardType.OVERVIEW -> R.drawable.bg_card_overview
                StudyReportCardType.HOMEWORK -> R.drawable.bg_card_homework
                StudyReportCardType.ATTENDANCE -> R.drawable.bg_card_attendance
                StudyReportCardType.EXAM -> R.drawable.bg_card_exam
                StudyReportCardType.PARTICIPATION -> R.drawable.bg_card_participation
                StudyReportCardType.SUMMARY -> R.drawable.bg_card_summary
            }
            itemView.setBackgroundResource(backgroundRes)
        }
        
        private fun buildChartText(chartData: List<ChartData>): String {
            if (chartData.isEmpty()) return "📊 暂无数据"
            
            val maxValue = chartData.maxOfOrNull { it.value } ?: 100f
            val chartLines = mutableListOf<String>()
            
            // 添加图表标题
            chartLines.add("📈 数据趋势")
            chartLines.add("")
            
            chartData.forEach { data ->
                val barLength = ((data.value / maxValue) * 12).toInt().coerceAtLeast(1)
                val bar = "▰".repeat(barLength) + "▱".repeat(12 - barLength)
                val emoji = when {
                    data.value >= maxValue * 0.8 -> "🟢"
                    data.value >= maxValue * 0.6 -> "🟡"
                    else -> "🔴"
                }
                chartLines.add("$emoji ${data.label}: $bar ${data.value}")
            }
            
            return chartLines.joinToString("\n")
        }
        
        private fun getDataEmoji(key: String): String {
            return when {
                key.contains("学习时长") -> "⏰"
                key.contains("学习天数") -> "📅"
                key.contains("完成度") -> "✅"
                key.contains("已完成") -> "📝"
                key.contains("平均分") -> "📊"
                key.contains("提交率") -> "📤"
                key.contains("出勤率") -> "👥"
                key.contains("迟到") -> "⏰"
                key.contains("请假") -> "🏠"
                key.contains("期中") -> "📋"
                key.contains("期末") -> "📄"
                key.contains("平时") -> "📚"
                key.contains("发言") -> "💬"
                key.contains("提问") -> "❓"
                key.contains("参与度") -> "🎯"
                key.contains("掌握程度") -> "🎓"
                key.contains("薄弱环节") -> "⚠️"
                key.contains("建议") -> "💡"
                else -> "📌"
            }
        }
        
        private fun getTitleEmoji(type: StudyReportCardType): String {
            return when (type) {
                StudyReportCardType.OVERVIEW -> "📊"
                StudyReportCardType.HOMEWORK -> "📝"
                StudyReportCardType.ATTENDANCE -> "👥"
                StudyReportCardType.EXAM -> "📋"
                StudyReportCardType.PARTICIPATION -> "💬"
                StudyReportCardType.SUMMARY -> "🎓"
            }
        }
    }
}

// 自定义图表视图
class CustomChartView(context: android.content.Context) : View(context) {
    
    private var chartData = listOf<ChartData>()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val path = Path()
    
    fun setChartData(data: List<ChartData>) {
        chartData = data
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (chartData.isEmpty()) return
        
        try {
            val width = width.toFloat()
            val height = height.toFloat()
            
            if (width <= 0 || height <= 0) return
            
            val padding = 40f
            val chartWidth = width - 2 * padding
            val chartHeight = height - 2 * padding
            
            if (chartWidth <= 0 || chartHeight <= 0) return
            
            // 绘制背景网格
            drawGrid(canvas, padding, chartWidth, chartHeight)
            
            // 绘制柱状图
            drawBarChart(canvas, padding, chartWidth, chartHeight)
            
            // 绘制标签
            drawLabels(canvas, padding, chartWidth, chartHeight)
        } catch (e: Exception) {
            // 如果绘制出错，至少不会崩溃
            e.printStackTrace()
        }
    }
    
    private fun drawGrid(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        paint.color = ContextCompat.getColor(context, R.color.chart_grid)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        
        // 绘制水平网格线
        for (i in 0..4) {
            val y = padding + (chartHeight / 4) * i
            canvas.drawLine(padding, y, padding + chartWidth, y, paint)
        }
        
        // 绘制垂直网格线
        for (i in 0..chartData.size) {
            val x = padding + (chartWidth / chartData.size) * i
            canvas.drawLine(x, padding, x, padding + chartHeight, paint)
        }
    }
    
    private fun drawBarChart(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        if (chartData.isEmpty()) return
        
        val maxValue = chartData.maxOfOrNull { it.value } ?: 100f
        if (maxValue <= 0) return
        
        val barWidth = chartWidth / chartData.size * 0.6f
        val barSpacing = chartWidth / chartData.size * 0.4f
        
        paint.style = Paint.Style.FILL
        
        chartData.forEachIndexed { index, data ->
            val barHeight = (data.value / maxValue) * chartHeight
            val left = padding + index * (barWidth + barSpacing) + barSpacing / 2
            val top = padding + chartHeight - barHeight
            val right = left + barWidth
            val bottom = padding + chartHeight
            
            // 设置渐变色
            paint.color = getBarColor(index)
            
            // 绘制圆角矩形
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, 8f, 8f, paint)
        }
    }
    
    private fun drawLabels(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        if (chartData.isEmpty()) return
        
        paint.color = ContextCompat.getColor(context, R.color.chart_label)
        paint.textSize = 24f
        paint.style = Paint.Style.FILL
        
        chartData.forEachIndexed { index, data ->
            val x = padding + index * (chartWidth / chartData.size) + (chartWidth / chartData.size) / 2
            val y = padding + chartHeight + 30f
            
            // 居中绘制文本
            val textWidth = paint.measureText(data.label)
            canvas.drawText(data.label, x - textWidth / 2, y, paint)
        }
    }
    
    private fun getBarColor(index: Int): Int {
        val colors = listOf(
            R.color.chart_bar_1,
            R.color.chart_bar_2,
            R.color.chart_bar_3,
            R.color.chart_bar_4,
            R.color.chart_bar_5
        )
        return ContextCompat.getColor(context, colors[index % colors.size])
    }
}