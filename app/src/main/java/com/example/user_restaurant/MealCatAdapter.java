package com.example.user_restaurant;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user_restaurant.fragments.MealCategoryDomain;

import java.util.ArrayList;

public class MealCatAdapter extends RecyclerView.Adapter<MealCatAdapter.ViewHolder> {

    ArrayList<MealCategoryDomain> mealCategoryDomains;
    private int selectedPosition = -1; // No selection initially

    public MealCatAdapter(ArrayList<MealCategoryDomain> mealCategoryDomains) {
        this.mealCategoryDomains = mealCategoryDomains;
    }

    @NonNull
    @Override
    public MealCatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_cat_viewholder, parent, false);

        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull MealCatAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mealcategoryName.setText(mealCategoryDomains.get(position).getTitle());

        // Apply selected/deselected styles
        if (position == selectedPosition) {
            holder.mealcategoryName.setBackgroundResource(R.drawable.meal_bg_black);
            holder.mealcategoryName.setTextColor(Color.WHITE);
        } else {
            holder.mealcategoryName.setBackgroundResource(R.drawable.black_box);
            holder.mealcategoryName.setTextColor(Color.BLACK);
        }

        // Click listener for item selection
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // Deselect if clicked again
                selectedPosition = -1;
            } else {
                // Deselect previous and select new
                notifyItemChanged(selectedPosition);
                selectedPosition = position;
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return mealCategoryDomains.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mealcategoryName;
        ConstraintLayout mealMainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mealcategoryName = itemView.findViewById(R.id.textView24);
            mealMainLayout = itemView.findViewById(R.id.mealmainlayout);
        }
    }
}
