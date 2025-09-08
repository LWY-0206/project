package com.example.loding.plaza;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import com.example.loding.R;
import com.example.loding.adapter.TopAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
//这个是顶部
public class TopFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private TopAdapter mTopAdapter;

    public TopFragment() {
        // Required empty public constructor
    }

    public static TopFragment newInstance() {
        return new TopFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top, container, false);
        initView(view);
        initViewPager();
        return view;
    }

    private void initView(View view) {
        // 初始化TabLayout和ViewPager2
        mTabLayout = view.findViewById(R.id.tabLayout);
        mViewPager = view.findViewById(R.id.sub_view_pager);
    }

    private void initViewPager() {
        // 创建适配器
        mTopAdapter = new TopAdapter(getActivity());
        // 设置ViewPager2的适配器
        mViewPager.setAdapter(mTopAdapter);

        // 连接TabLayout和ViewPager2
        new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // 从适配器获取标签文本并设置
                tab.setText(mTopAdapter.getTabTitle(position));
            }
        }).attach();
    }
}