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
        
        // 为搜索框添加文本变化监听器
        binding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 文本变化前的处理
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 根据文本内容控制清除按钮的显示和隐藏
                binding.clearButton.visibility = if (s?.isNotEmpty() == true) android.view.View.VISIBLE else android.view.View.GONE
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {
                val keyword = s.toString().trim()
                // 当文本为空时，显示所有课程；否则实时搜索
                if (keyword.isEmpty()) {
                    if (identity == 1) {
                        adapter.submitTeacherList(viewModel.teacherCourseList.value)
                    } else {
                        adapter.submitList(viewModel.courseList.value)
                    }
                } else {
                    performSearch(keyword)
                }
            }
        })
        
        // 为清除按钮添加点击事件
        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            // 显示所有课程
            if (identity == 1) {
                adapter.submitTeacherList(viewModel.teacherCourseList.value)
            } else {
                adapter.submitList(viewModel.courseList.value)
            }
            // 隐藏软键盘
            val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }
        
        // 设置搜索按钮的IME选项监听器
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.searchEditText.text.toString().trim()
                performSearch(keyword)
                // 隐藏软键盘
                val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
        
        return binding.root
    }

    /**
     * 执行搜索功能
     */
    private fun performSearch(keyword: String) {
        if (identity == 1) {
            // 老师身份搜索
            val searchResult = viewModel.teacherCourseList.value?.filter {
                it.subjectName.contains(keyword, true)
            }
            adapter.submitTeacherList(searchResult)
        } else {
            // 学生身份搜索
            val searchResult = viewModel.courseList.value?.filter {
                it.subjectName.contains(keyword, true) || it.teacherName.contains(keyword, true)
            }
            adapter.submitList(searchResult)
        }
        
        // 如果搜索结果为空，显示提示
        if ((identity == 1 && adapter.itemCount == 0) || (identity == 0 && adapter.itemCount == 0)) {
            android.widget.Toast.makeText(requireContext(), "未找到相关课程", android.widget.Toast.LENGTH_SHORT).show()
        }
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
                intent.putExtra("subjectName", it.subjectName)
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

