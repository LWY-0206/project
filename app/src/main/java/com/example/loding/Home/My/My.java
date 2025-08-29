package com.example.loding.Home.My;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.loding.Home.ProfileActivity;
import com.example.loding.R;
import com.example.loding.databinding.FragmentMyBinding;


public class My extends Fragment {
    FragmentMyBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentMyBinding.inflate(inflater,container,false);
        setupListeners();
        return binding.getRoot();
    }
    private void setupListeners(){
        binding.navProfile.setOnClickListener(v -> {
            // 处理点击事件
            Intent intent=new Intent(requireActivity(), ProfileActivity.class);
            startActivity(intent);
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 避免内存泄漏
    }
}