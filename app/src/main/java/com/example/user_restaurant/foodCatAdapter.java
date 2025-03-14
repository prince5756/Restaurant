package com.example.user_restaurant;

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
import com.example.user_restaurant.fragments.FoodCategoryDomain;

import java.util.ArrayList;

public class foodCatAdapter extends RecyclerView.Adapter<foodCatAdapter.ViewHolder> {

    ArrayList<FoodCategoryDomain> foodCategoryDomains;

    public foodCatAdapter(ArrayList<FoodCategoryDomain> foodCategoryDomains){
        this.foodCategoryDomains = foodCategoryDomains;
    }

    @NonNull
    @Override
    public foodCatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_cat_viewholder , parent, false);

        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull foodCatAdapter.ViewHolder holder, int position) {
        holder.categoryName.setText(foodCategoryDomains.get(position).getTitle());
        String picUrl = "";
        switch (position){
            case 0:{
                picUrl = "vegetarian";
                break;
            }
            case 1:{
                picUrl = "non_veg";
                break;
            }
            case 2:{
                picUrl = "vegan";
                break;
            }
            case 3:{
                picUrl = "jain";
                break;
            }
        }

        int drawableResourceId = holder.itemView.getContext().getResources().getIdentifier(picUrl, "drawable", holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext()).load(drawableResourceId).into(holder.categoryPic);
    }

    @Override
    public int getItemCount() {
        return foodCategoryDomains.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        ImageView categoryPic;
        ConstraintLayout mainlayout;
        View view2;
        CardView card_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryPic = itemView.findViewById(R.id.categoryPic);
            mainlayout = itemView.findViewById(R.id.mainlayout);
            //view2 = itemView.findViewById(R.id.view2);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }
}
