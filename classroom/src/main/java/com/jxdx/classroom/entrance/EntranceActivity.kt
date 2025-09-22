package com.jxdx.classroom.entrance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxdx.classroom.R

class EntranceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance_fragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EntranceFragment())
                .commit()
        }
    }
}