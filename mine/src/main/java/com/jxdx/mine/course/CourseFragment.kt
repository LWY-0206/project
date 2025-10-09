package com.jxdx.mine.course

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import com.jxdx.mine.databinding.FragmentCourseListBinding

class CourseFragment : Fragment() {

    private lateinit var binding: FragmentCourseListBinding
    private lateinit var adapter: CourseAdapter
    private val viewModel: CourseViewModel by viewModels()
    private var identity=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseListBinding.inflate(inflater, container, false)
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        identity=activity?.intent?.getIntExtra("identity",0)?:0
        if(identity==1){
            //如果是老师，显示create_course
            binding.createCourse.visibility = View.VISIBLE
        }else{
            binding.createCourse.visibility=View.GONE
        }
        binding.createCourse.setOnClickListener {
            val intent = Intent(requireContext(), CreateCourseActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CourseAdapter(
            identity,
            {
                // 学生点击课程
                val intent = Intent(requireContext(), CourseListActivity::class.java)
                intent.putExtra("courseId", it.subjectId)
                intent.putExtra("subjectName", it.subjectName)
                intent.putExtra("teacherName", it.teacherName)
                intent.putExtra("identity", identity)
                startActivity(intent)
            },
            {
                // 老师点击学科
                val intent = Intent(requireContext(), CourseListActivity::class.java)
                intent.putExtra("courseId", it.subjectId)
                intent.putExtra("identity", identity)
                startActivity(intent)
            }
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if(identity==0){//学生
            viewModel.courseList.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
            }
            viewModel.loadCourses()
        }else{//老师
            viewModel.teacherCourseList.observe(viewLifecycleOwner) { list ->
                adapter.submitTeacherList(list)
            }
            // 老师身份调用getTeacherSubject接口
            viewModel.loadCourses()
        }
    }
}

