package com.jxdx.mine.homework

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxdx.mine.R
import com.jxdx.mine.Homework
import com.jxdx.mine.SubjectGroup
import com.jxdx.mine.adapter.HomeworkAdapter
import com.jxdx.mine.adapter.HomeworkRepository
import com.jxdx.mine.adapter.HomeworkViewModel
import com.jxdx.mine.databinding.FragmentHomeworkListBinding
import com.jxdx.mine.homework.HomeworkDetailActivity

class HomeworkListFragment : Fragment() {
    private lateinit var binding: FragmentHomeworkListBinding
    private lateinit var viewModel: HomeworkViewModel
    private lateinit var adapter: HomeworkAdapter
    private lateinit var recyclerView: RecyclerView
    
    // 用于接收HomeworkDetailActivity返回结果的启动器
    private lateinit var detailLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 在onCreate中初始化ActivityResultLauncher
        detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 如果操作成功，重新加载作业列表以更新状态
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val status = arguments?.getInt("status", -1) ?: -1
                val finalStatus = if (status in 0..2) status else 0
                viewModel.loadHomework(finalStatus)
            }
        }
    }

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

        // 设置作业项点击事件
        adapter.setOnItemClickListener { 
            val homeworkId = it.homeworkId.toIntOrNull() ?: return@setOnItemClickListener
            val intent = Intent(requireContext(), HomeworkDetailActivity::class.java)
            intent.putExtra("homeworkId", homeworkId)
            // 使用launcher启动Activity，而不是直接startActivity
            detailLauncher.launch(intent)
        }

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
        
        val finalStatus = if (status in 0..2) status else 0
        Log.d("HomeworkListFragment", "Loading homework with status: $finalStatus")
        viewModel.loadHomework(finalStatus)
        
        // 滚动监听，滑动到底部加载更多
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    // 加载下一页
                    viewModel.loadHomework(finalStatus, isLoadMore = true)
                }
            }
        })
    }
}
