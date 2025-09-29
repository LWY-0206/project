package com.jxdx.mine.course

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.mine.R

class CourseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CourseFragment())
                .commit()
        }
    }
}