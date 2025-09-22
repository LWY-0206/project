package com.jxdx.mine.grade

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.Member
import com.jxdx.mine.R
import com.jxdx.mine.adapter.ClassMemberAdapter
class GradeActivity : AppCompatActivity() {

    private lateinit var adapter: ClassMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        //上级跳转传入用户的班级信息
        val className = intent.getStringExtra("CLASS_NAME") ?: "高一1班"


        findViewById<TextView>(R.id.tv_class_name).text = className
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_member_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ClassMemberAdapter { member ->
            Toast.makeText(this, "点击：${member.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        loadMockData()
    }

    private fun loadMockData() {
        val list = mutableListOf<Member>()

        // Section: 老师
        list.add(Member("section_teacher", "老师", "","",0))
        list.add(Member("t1", "张老师", "https://example.com/teacher1.png", "班主任", 1))
        list.add(Member("t2", "李老师", "https://example.com/teacher2.png", "数学老师", 1))

        // Section: 学生
        list.add(Member("section_student", "学生", "","",0))
        list.add(Member("s1", "王小明", "https://example.com/student1.png", "",2))
        list.add(Member("s2", "李华", "https://example.com/student2.png","",2))
        list.add(Member("s3", "赵六", "https://example.com/student2.png", "",2))

        adapter.submitList(list)
    }
}
