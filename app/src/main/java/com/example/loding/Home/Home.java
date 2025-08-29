package com.example.loding.Home;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.loding.Home.My.My;
import com.example.loding.R;

public class Home extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    private LinearLayout navHome, navSquare, navClass, navResource, navProfile;
    private ImageView ivHome, ivSquare, ivClass, ivResource, ivProfile;
    private TextView tvHome, tvSquare, tvClassTextView, tvResource, tvProfile;

    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupClickListeners();

        // 默认显示首页
        switchFragment(currentTab);
    }

    private void initViews() {
        fragmentContainer = findViewById(R.id.fragment_container);

        navHome = findViewById(R.id.nav_home);
        navSquare = findViewById(R.id.nav_square);
        navClass = findViewById(R.id.nav_class); // 圆形按钮
        navResource = findViewById(R.id.nav_resource);
        navProfile = findViewById(R.id.nav_profile);

        ivHome = findViewById(R.id.iv_home);
        ivSquare = findViewById(R.id.iv_square);
        ivClass = findViewById(R.id.iv_class);
        ivResource = findViewById(R.id.iv_resource);
        ivProfile = findViewById(R.id.iv_profile);

        // 获取文本视图
        tvHome = (TextView) navHome.getChildAt(1);
        tvSquare = (TextView) navSquare.getChildAt(1);
        tvClassTextView = (TextView) navClass.getChildAt(1); // 圆形按钮中的文本
        tvResource = (TextView) navResource.getChildAt(1);
        tvProfile = (TextView) navProfile.getChildAt(1);
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> switchFragment(0));
        navSquare.setOnClickListener(v -> switchFragment(1));
        navClass.setOnClickListener(v -> switchFragment(2));
        navResource.setOnClickListener(v -> switchFragment(3));
        navProfile.setOnClickListener(v -> switchFragment(4));
    }

    private void switchFragment(int position) {
        currentTab = position;
        resetAllTabs();

        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new My();
                ivHome.setImageResource(R.drawable.ketang);
                tvHome.setTextColor(getResources().getColor(R.color.purple_200));
                break;
            case 1:
                fragment = new My();
                ivSquare.setImageResource(R.drawable.ketang);
                tvSquare.setTextColor(getResources().getColor(R.color.purple_200));
                break;
            case 2:
                fragment = new My();
                // 设置圆形按钮的选中状态
                navClass.setBackgroundResource(R.drawable.bg_circle_primary_selected);
                ivClass.setImageResource(R.drawable.ketang);
                tvClassTextView.setTextColor(getResources().getColor(R.color.purple_200));
                break;
            case 3:
                fragment = new My();
                ivResource.setImageResource(R.drawable.ketang);
                tvResource.setTextColor(getResources().getColor(R.color.purple_200));
                break;
            case 4:
                fragment = new My();
                ivProfile.setImageResource(R.drawable.ketang);
                tvProfile.setTextColor(getResources().getColor(R.color.purple_200));
                break;
            default:
                fragment = new My();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void resetAllTabs() {
        // 重置所有图标和文字颜色
        ivHome.setImageResource(R.drawable.ketang);
        ivSquare.setImageResource(R.drawable.ketang);
        ivResource.setImageResource(R.drawable.ketang);
        ivProfile.setImageResource(R.drawable.ketang);


        tvHome.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvSquare.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvResource.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvProfile.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // 重置圆形按钮
        navClass.setBackgroundResource(R.drawable.bg_circle_primary);
        ivClass.setImageResource(R.drawable.ketang);

        tvClassTextView.setTextColor(getResources().getColor(android.R.color.white));
    }
}