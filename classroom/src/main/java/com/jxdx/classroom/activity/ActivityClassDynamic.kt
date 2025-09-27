package com.jxdx.classroom.activity

import android.content.Intent
import com.example.corekit.common.BaseActivity
import com.jxdx.classroom.databinding.ActivityClassdynamicBinding

class ActivityClassDynamic: BaseActivity<ActivityClassdynamicBinding>() {
    override fun bindLayout(): ActivityClassdynamicBinding {
        return ActivityClassdynamicBinding.inflate(layoutInflater)
    }

    override fun initView() {
        view.btnStartLive.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun subscribeUi() {
    }
}