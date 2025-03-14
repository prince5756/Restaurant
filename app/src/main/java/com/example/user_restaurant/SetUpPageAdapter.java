package com.example.user_restaurant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.user_restaurant.fragments.AddressTab;
import com.example.user_restaurant.fragments.MenuTab;
import com.example.user_restaurant.fragments.TablesTab;

public class SetUpPageAdapter extends FragmentStateAdapter {
    public SetUpPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new TablesTab();
            case 1: return new MenuTab();
            case 2: return new AddressTab();
            default :return new TablesTab();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
