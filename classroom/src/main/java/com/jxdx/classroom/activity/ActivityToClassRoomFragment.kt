package com.jxdx.classroom.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jxdx.classroom.R
import com.jxdx.classroom.entrance.EntranceFragment
import com.jxdx.classroom.fragment.ClassRoomFragment

class ActivityToClassRoomFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_class_room_fragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ClassRoomFragment())
                .commit()
        }
    }
}