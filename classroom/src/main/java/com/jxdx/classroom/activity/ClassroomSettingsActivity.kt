package com.jxdx.classroom.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityClassroomSettingsBinding
import com.jxdx.classroom.entity.ClassroomSetting
import com.jxdx.classroom.viewmodel.ClassroomSettingsViewModel

class ClassroomSettingsActivity : BaseActivity<ActivityClassroomSettingsBinding>() {

    private lateinit var viewModel: ClassroomSettingsViewModel
    
    private var subjectId: Int = 0
    private var teacherId: Int = 0
    private var subjectName: String = ""

    override fun bindLayout(): ActivityClassroomSettingsBinding {
        return ActivityClassroomSettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 接收Intent传递的数据
        intent?.let {
            subjectId = it.getIntExtra("subjectId", 0)
            teacherId = it.getIntExtra("teacherId", 0)
            subjectName = it.getStringExtra("subjectName") ?: ""
            Log.d("ClassroomSettingsActivity", "接收到的参数: subjectId=$subjectId, teacherId=$teacherId, subjectName=$subjectName")
        }

        // 设置标题
        if (subjectName.isNotEmpty()) {
            view.tvTitle.text = "$subjectName - 课堂设置"
        }

        // 返回按钮
        view.btnBack.setOnClickListener {
            finish()
        }

        // 保存设置按钮
        view.btnSave.setOnClickListener {
            val settings = getCurrentSettings()
            viewModel.saveSettings(settings)
        }

        // 重置设置按钮
        view.btnReset.setOnClickListener {
            resetToDefaults()
        }

        // 初始化ViewModel
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ClassroomSettingsViewModel::class.java]
    }

    override fun subscribeUi() {
        // 观察设置数据
        viewModel.classroomSettings.observe(this) { settings ->
            updateUI(settings)
        }

        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            view.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        // 观察错误信息
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        // 观察保存结果
        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "课堂设置保存成功", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 加载设置数据
        viewModel.loadSettings(subjectId, teacherId)
    }

    private fun getCurrentSettings(): ClassroomSetting {
        return ClassroomSetting(
            subjectId = subjectId,
            teacherId = teacherId,
            allowStudentChat = view.switchStudentChat.isChecked,
            allowStudentRaiseHand = view.switchStudentRaiseHand.isChecked,
            allowStudentDraw = view.switchStudentDraw.isChecked,
            autoMuteStudents = view.switchAutoMuteStudents.isChecked,
            enableRecording = view.switchEnableRecording.isChecked,
            enableScreenShare = view.switchEnableScreenShare.isChecked,
            maxStudents = view.seekBarMaxStudents.progress + 1,
            classDuration = view.seekBarClassDuration.progress + 30, // 30-120分钟
            breakTime = view.seekBarBreakTime.progress * 5 // 0-30分钟，每5分钟一个间隔
        )
    }

    private fun updateUI(settings: ClassroomSetting) {
        view.switchStudentChat.isChecked = settings.allowStudentChat
        view.switchStudentRaiseHand.isChecked = settings.allowStudentRaiseHand
        view.switchStudentDraw.isChecked = settings.allowStudentDraw
        view.switchAutoMuteStudents.isChecked = settings.autoMuteStudents
        view.switchEnableRecording.isChecked = settings.enableRecording
        view.switchEnableScreenShare.isChecked = settings.enableScreenShare
        
        view.seekBarMaxStudents.progress = settings.maxStudents - 1
        view.seekBarClassDuration.progress = settings.classDuration - 30
        view.seekBarBreakTime.progress = settings.breakTime / 5
        
        updateLabels()
    }

    private fun resetToDefaults() {
        val defaultSettings = ClassroomSetting.getDefaultSettings(subjectId, teacherId)
        updateUI(defaultSettings)
        Toast.makeText(this, "已重置为默认设置", Toast.LENGTH_SHORT).show()
    }

    private fun updateLabels() {
        view.tvMaxStudentsValue.text = "${view.seekBarMaxStudents.progress + 1}人"
        view.tvClassDurationValue.text = "${view.seekBarClassDuration.progress + 30}分钟"
        view.tvBreakTimeValue.text = "${view.seekBarBreakTime.progress * 5}分钟"
    }
}
