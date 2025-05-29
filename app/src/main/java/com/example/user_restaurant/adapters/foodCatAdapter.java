package com.example.user_restaurant.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;
import com.example.user_restaurant.models.FoodCategoryDomain;

import java.util.ArrayList;

public class foodCatAdapter extends RecyclerView.Adapter<foodCatAdapter.ViewHolder> {

    ArrayList<FoodCategoryDomain> foodCategoryDomains;
    private int selectedPosition = -1;
    private OnFoodCategoryClickListener listener;

    public interface OnFoodCategoryClickListener {
        void onFoodCategoryClick(FoodCategoryDomain category, boolean isSelected);
    }

    public void setOnFoodCategoryClickListener(OnFoodCategoryClickListener listener) {
        this.listener = listener;
    }

    public foodCatAdapter(ArrayList<FoodCategoryDomain> foodCategoryDomains) {
        this.foodCategoryDomains = foodCategoryDomains;
    }

    @NonNull
    @Override
    public foodCatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_cat_viewholder, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull foodCatAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FoodCategoryDomain category = foodCategoryDomains.get(position);
        holder.categoryName.setText(category.getTitle());
        String picUrl = "";
        switch (position) {
            case 0:
                picUrl = "vegetarian";
                break;
            case 1:
                picUrl = "non_veg";
                break;
            case 2:
                picUrl = "vegan";
                break;
            case 3:
                picUrl = "jain";
                break;
        }
        int drawableResourceId = holder.itemView.getContext()
                .getResources().getIdentifier(picUrl, "drawable", holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext()).load(drawableResourceId).into(holder.categoryPic);

        // New theme: Use deep orange with white text when selected;
        // otherwise, use light gray background with dark gray text.
        if (position == selectedPosition) {
            holder.card_view.setCardBackgroundColor(Color.parseColor("#FF5722")); // Deep orange
            holder.categoryName.setTextColor(Color.WHITE);
        } else {
            holder.card_view.setCardBackgroundColor(Color.parseColor("#FAFAFA")); // Light gray
            holder.categoryName.setTextColor(Color.parseColor("#212121"));    // Dark gray
        }

        // Toggle selection on item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition == position) {
                    // Deselect if clicked again
                    selectedPosition = -1;
                    if (listener != null) {
                        listener.onFoodCategoryClick(category, false);
                    }
                } else {
                    int previousPosition = selectedPosition;
                    selectedPosition = position;
                    if (listener != null) {
                        listener.onFoodCategoryClick(category, true);
                    }
                    if (previousPosition != -1) {
                        notifyItemChanged(previousPosition);
                    }
                }
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodCategoryDomains.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryPic;
        ConstraintLayout mainlayout;
        CardView card_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryPic = itemView.findViewById(R.id.categoryPic);
            mainlayout = itemView.findViewById(R.id.mainlayout);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }
}
