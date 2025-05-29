package com.example.user_restaurant.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user_restaurant.models.ModelMenu;
import com.example.user_restaurant.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MealCatAdapter extends RecyclerView.Adapter<MealCatAdapter.ViewHolder> {
    private List<String> mealTypes;
    private int selectedPosition = -1;
    private OnMealTypeClickListener listener;

    public interface OnMealTypeClickListener {
        void onMealTypeClick(String mealType, boolean isSelected);
    }

    // Construct the adapter using the full menu list (to extract unique meal types)
    public MealCatAdapter(ArrayList<ModelMenu> fullMenuList) {
        updateData(fullMenuList);
    }

    // Update the list of unique meal types from the full menu list
    public void updateData(ArrayList<ModelMenu> fullMenuList) {
        Set<String> set = new LinkedHashSet<>();
        for (ModelMenu item : fullMenuList) {
            if (item.getMealType() != null) {
                set.add(item.getMealType());
            }
        }
        mealTypes = new ArrayList<>(set);
        selectedPosition = -1; // Reset selection when data is updated
        notifyDataSetChanged();
    }

    public void setOnMealTypeClickListener(OnMealTypeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealCatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_cat_viewholder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealCatAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String mealType = mealTypes.get(position);
        holder.mealTypeTextView.setText(mealType);

        // Apply selected/deselected styles
        if (position == selectedPosition) {
            holder.mealTypeTextView.setBackgroundResource(R.drawable.meal_bg_black);
            holder.mealTypeTextView.setTextColor(Color.WHITE);
        } else {
            holder.mealTypeTextView.setBackgroundResource(R.drawable.black_box);
            holder.mealTypeTextView.setTextColor(Color.BLACK);
        }

        // Click listener for filtering callback
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // Deselect: show all menu items
                selectedPosition = -1;
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onMealTypeClick(mealType, false);
                }
            } else {
                int previousPosition = selectedPosition;
                selectedPosition = position;
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onMealTypeClick(mealType, true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealTypes != null ? mealTypes.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealTypeTextView;
        ConstraintLayout mealMainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mealTypeTextView = itemView.findViewById(R.id.textView24);
            mealMainLayout = itemView.findViewById(R.id.mealmainlayout);
        }
    }
}
