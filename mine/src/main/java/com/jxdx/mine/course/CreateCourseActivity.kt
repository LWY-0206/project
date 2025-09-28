package com.jxdx.mine.course

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.mine.databinding.ActivityCreateCourseBinding

/** 课程数据模型 */
data class SubjectCourse(
    val classes: List<String>,   // 多个班级
    val courseName: String,      // 课程名称
    val description: String,     // 课程简介
    val teacherName: String      // 老师姓名
)

class CreateCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCourseBinding

    /** 可选择的班级列表 */
    private val allClasses = arrayOf(
        "软件111", "软件222", "软件333", "软件444",
        "计科111", "计科 222", "计科333", "计科444",
        "网络111", "网络222","网络333", "网络444",
    )

    /** 保存当前已选择的班级索引 */
    private val selectedClassIndexes = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "创建课程"

        setupListeners()
    }

    private fun setupListeners() {
        // 多选班级
        binding.btnSelectClasses.setOnClickListener {
            showMultiSelectClassDialog()
        }

        // 保存按钮
        binding.btnSave.setOnClickListener {
            onSaveCourse()
        }

        //返回按钮
        binding.activityCreateCourseBack.setOnClickListener {
            finish()
        }
    }

    /** 显示多选对话框 */
    private fun showMultiSelectClassDialog() {
        val checkedItems = BooleanArray(allClasses.size) { i ->
            selectedClassIndexes.contains(i)
        }

        AlertDialog.Builder(this)
            .setTitle("选择班级")
            .setMultiChoiceItems(allClasses, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    if (!selectedClassIndexes.contains(which)) {
                        selectedClassIndexes.add(which)
                    }
                } else {
                    selectedClassIndexes.remove(which)
                }
            }
            .setPositiveButton("确定") { dialog, _ ->
                updateSelectedClassesText()
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 更新显示已选班级 */
    private fun updateSelectedClassesText() {
        val selectedNames = selectedClassIndexes.sorted().map { allClasses[it] }
        binding.tvSelectedClasses.text = if (selectedNames.isEmpty()) {
            "未选择班级"
        } else {
            "已选择: ${selectedNames.joinToString("、")}"
        }
    }

    /** 保存课程 */
    private fun onSaveCourse() {
        if (selectedClassIndexes.isEmpty()) {
            Toast.makeText(this, "请至少选择一个班级", Toast.LENGTH_SHORT).show()
            return
        }

        val courseName = binding.etCourseName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val teacherName = binding.etTeacherName.text.toString().trim()

        if (courseName.isEmpty()) {
            binding.etCourseName.error = "请输入课程名称"
            return
        }
        if (teacherName.isEmpty()) {
            binding.etTeacherName.error = "请输入授课老师姓名"
            return
        }

        val selectedNames = selectedClassIndexes.sorted().map { allClasses[it] }

        val course = SubjectCourse(
            classes = selectedNames,
            courseName = courseName,
            description = description,
            teacherName = teacherName
        )
        Log.d("CreateSubjectCourse", "提交的课程数据: $course")
        Toast.makeText(this, "课程已创建：${course.courseName}", Toast.LENGTH_LONG).show()

        // finish() // 创建完成后关闭页面
    }
}
