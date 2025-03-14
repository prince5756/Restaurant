package com.example.user_restaurant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.user_restaurant.fragments.HisOdrDetail;
import com.example.user_restaurant.fragments.bookfragment;
import com.example.user_restaurant.fragments.complete_orders;
import com.example.user_restaurant.fragments.pending_oders;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull bookfragment fragmentActivity) {
        super(fragmentActivity);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new pending_oders();
            case 1: return new complete_orders();
            case 2: return new HisOdrDetail();
            default: return new pending_oders();
        }
    }

    @NonNull
    @Override
    public int getItemCount() {
        return 3;
    }
}
