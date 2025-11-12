package org.jxxy.debug.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * 日期时间计算工具类
 * 支持日期操作、格式化、工作日计算等复杂场景
 * 线程安全，所有方法均为无状态操作
 */
object CalculatorUtil {
    private val DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * 日期加减操作（支持天、周、月、年）
     * @param date 原始日期
     * @param amount 数量（可正负）
     * @param unit 时间单位（ChronoUnit.DAYS/WEEKS/MONTHS/YEARS）
     */
    fun add(date: LocalDate, amount: Long, unit: ChronoUnit): LocalDate = when (unit) {
        ChronoUnit.DAYS -> date.plusDays(amount)
        ChronoUnit.WEEKS -> date.plusWeeks(amount)
        ChronoUnit.MONTHS -> date.plusMonths(amount)
        ChronoUnit.YEARS -> date.plusYears(amount)
        else -> throw IllegalArgumentException("Unsupported unit: $unit")
    }

    /**
     * 计算两个日期的差值
     * @param unit 时间单位（ChronoUnit.DAYS/MONTHS/YEARS等）
     * @return 如果 start > end 返回负值
     */
    fun between(start: LocalDate, end: LocalDate, unit: ChronoUnit): Long {
        return unit.between(start, end)
    }

    /**
     * 安全解析日期字符串（支持多格式自动识别）
     * @param patterns 尝试的格式列表，默认支持["yyyy-MM-dd", "yyyy/MM/dd"]
     * @return 解析成功返回LocalDate，失败返回null
     */
    fun String.toLocalDateSafe(vararg patterns: String = arrayOf("yyyy-MM-dd", "yyyy/MM/dd")): LocalDate? {
        return patterns.firstNotNullOfOrNull { pattern ->
            try {
                LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }

    /**
     * 智能月份加减（处理月末日期问题）
     * 示例：2023-01-31 加1个月 → 2023-02-28
     */
    fun addMonthsSafely(date: LocalDate, months: Long): LocalDate {
        val adjusted = date.plusMonths(months)
        return if (date.dayOfMonth == date.lengthOfMonth()) {
            adjusted.withDayOfMonth(adjusted.lengthOfMonth())
        } else {
            adjusted
        }
    }

    /**
     * 计算过期日期（智能处理月末日期）
     * @param productionDate 生产日期
     * @param shelfLife 保质期数值（必须 >0）
     * @param unit 时间单位（DAYS/MONTHS/YEARS）
     * @param includeProductionDay 是否包含生产当天（默认true）
     * @throws IllegalArgumentException 参数非法时抛出
     */
    fun calculateExpiryDate(
        productionDate: LocalDate,
        shelfLife: Long,
        unit: ChronoUnit,
        includeProductionDay: Boolean = true
    ): LocalDate {
        require(shelfLife > 0) { "Shelf life must be positive" }
        require(!productionDate.isAfter(LocalDate.now())) { "Production date cannot be in the future" }

        return when (unit) {
            ChronoUnit.MONTHS -> addMonthsSafely(
                if (includeProductionDay) productionDate else productionDate.plusDays(1),
                shelfLife
            )
            ChronoUnit.YEARS -> addMonthsSafely(
                if (includeProductionDay) productionDate else productionDate.plusDays(1),
                shelfLife * 12
            )
            else -> add(
                if (includeProductionDay) productionDate else productionDate.plusDays(1),
                shelfLife,
                unit
            )
        }
    }

    /**
     * 计算剩余有效天数（负数表示已过期）
     * @param expiryDate 过期日期
     */
    fun remainingDays(expiryDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
    }

    /**
     * 获取保质期状态（含提醒阈值）
     * @param alertDays 提前提醒天数（默认7天）
     * @return 状态枚举 ExpiryStatus
     */
    fun getExpiryStatus(expiryDate: LocalDate, alertDays: Int = 7): ExpiryStatus {
        val remaining = remainingDays(expiryDate)
        return when {
            remaining < 0 -> ExpiryStatus.EXPIRED
            remaining <= alertDays -> ExpiryStatus.ALERT
            else -> ExpiryStatus.NORMAL
        }
    }

    enum class ExpiryStatus { NORMAL, ALERT, EXPIRED }



    /**
     * 解析用户输入日期（支持模糊格式）
     * 示例："2023年5月1日" → 2023-05-01
     */
    fun parseUserInputDate(input: String): LocalDate? {
        return input.toLocalDateSafe(
            "yyyy-MM-dd", "yyyy/MM/dd",
            "yyyy年MM月dd日", "yyyy.MM.dd",
        )
    }


    fun LocalDate.toFriendlyString(): String {
        val formattedMonth = String.format("%02d", monthValue)
        val formattedDay = String.format("%02d", dayOfMonth)
        return "${year}-${formattedMonth}-${formattedDay}"
    }
}