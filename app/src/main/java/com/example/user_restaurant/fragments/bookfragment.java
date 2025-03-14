package com.example.user_restaurant.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.user_restaurant.R;
import com.example.user_restaurant.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class bookfragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
//    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the merged layout
        return inflater.inflate(R.layout.fragment_bookfragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        // Initialize SwipeRefreshLayout and set a refresh listener
//        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            // Refresh logic: notify the adapter that data may have changed
//            viewPagerAdapter.notifyDataSetChanged();
//            // End the refreshing animation
//            swipeRefreshLayout.setRefreshing(false);
//        });

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager);

        // Set up the ViewPagerAdapter for ViewPager2
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);

        // Attach TabLayout to ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if (position == 0) {
                tab.setText("Upcoming Bookings");
            } else if (position == 1) {
                tab.setText("Today's Bookings");
            } else if (position == 2) {
                tab.setText("Bookings History");
            }
        }).attach();

        // Optional: Customize tab selection behavior
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View tabView = tab.view;
                tabView.setBackgroundResource(R.drawable.tab_curved_bg);
                viewPager2.setVisibility(View.VISIBLE);
                viewPager2.setCurrentItem(tab.getPosition());
                tabLayout.setTabTextColors(
                        getResources().getColor(R.color.unselected_txt_color),
                        getResources().getColor(R.color.selected_txt_color)
                );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View tabView = tab.view;
                tabView.setBackgroundResource(R.drawable.tab_curved_removebg);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.VISIBLE);
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                super.onPageSelected(position);
            }
        });

        // Programmatically select the first tab and update its style
        TabLayout.Tab firstTab = tabLayout.getTabAt(0);
        if (firstTab != null) {
            firstTab.select();
            View firstTabView = firstTab.view;
            firstTabView.setBackgroundResource(R.drawable.tab_curved_bg);
            tabLayout.setTabTextColors(
                    getResources().getColor(R.color.unselected_txt_color),
                    getResources().getColor(R.color.selected_txt_color)
            );
        }
    }
}
