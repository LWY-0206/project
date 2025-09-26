package com.jxdx.classroom.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.classroom.R
import com.jxdx.classroom.com.jxdx.classroom.activity.ClassEnterFragment


class ActivityToClassRoomFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_class_room_fragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ClassEnterFragment())
                .commit()
        }
    }
}