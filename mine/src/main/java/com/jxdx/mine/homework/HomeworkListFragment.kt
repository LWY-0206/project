package com.jxdx.mine.homework

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.adapter.HomeworkAdapter
import com.jxdx.mine.adapter.HomeworkRepository
import com.jxdx.mine.adapter.HomeworkViewModel

class HomeworkListFragment : Fragment() {

    private lateinit var viewModel: HomeworkViewModel
    private lateinit var adapter: HomeworkAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_homework_list, container, false).apply {
            recyclerView = findViewById(R.id.recyclerView)
            Log.d("HomeworkListFragment", "RecyclerView initialized")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("HomeworkListFragment", "onViewCreated started")
        // 初始化适配器和RecyclerView
        adapter = HomeworkAdapter()
        Log.d("HomeworkListFragment", "Adapter created")
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("HomeworkListFragment", "RecyclerView configured")

        // 初始化ViewModel并传入HomeworkRepository
        viewModel = HomeworkViewModel(HomeworkRepository())
        Log.d("HomeworkListFragment", "ViewModel initialized")
        
        viewModel.homeworkLiveData.observe(viewLifecycleOwner) {
            Log.d("HomeworkListFragment", "LiveData observed: ${it?.size ?: 0} groups received")
            if (it != null) {
                // 打印每个分组的详细信息用于调试
                it.forEachIndexed { index, group ->
                    Log.d("HomeworkListFragment", "Group $index: ${group.subjectName}, expanded: ${group.isExpanded}, homework count: ${group.homeworkList.size}")
                }
                adapter.setData(it)
                Log.d("HomeworkListFragment", "Data set to adapter, item count: ${adapter.itemCount}")
            } else {
                Log.d("HomeworkListFragment", "No homework data received")
            }
        }

        // 读取传入的状态参数 - 使用更安全的方式
        val status = arguments?.getInt("status", -1) ?: -1
        Log.d("HomeworkListFragment", "Received status parameter: $status")
        
        if (status in 0..2) {
            Log.d("HomeworkListFragment", "Loading homework with status: $status")
            viewModel.loadHomework(status)
        } else {
            Log.d("HomeworkListFragment", "Invalid status parameter, using default 0 (未提交)")
            viewModel.loadHomework(0)
        }
    }
}
