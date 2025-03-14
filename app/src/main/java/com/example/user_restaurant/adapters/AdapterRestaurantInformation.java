package com.example.user_restaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.user_restaurant.R;
import com.example.user_restaurant.models.ModelMenu;

import java.util.List;

public class AdapterRestaurantInformation extends RecyclerView.Adapter<AdapterRestaurantInformation.MenuViewHolder> {

    private final Context context;
    private final List<ModelMenu> menuList;

    public AdapterRestaurantInformation(Context context, List<ModelMenu> menuList) {
        this.context = context;
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each menu item
        View view = LayoutInflater.from(context).inflate(R.layout.layout_restarant_information, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        ModelMenu menuItem = menuList.get(position);

        // Set item details
        holder.itemNameTextView.setText(menuItem.getItemName());
        holder.itemPriceTextView.setText(menuItem.getItemPrice());
        holder.itemDescriptionTextView.setText(menuItem.getItemDescription());

        // Display image slider for multiple images (use Glide)
        List<String> imageUrls = menuItem.getFoodImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            displayImageSlider(holder.itemImageSlider, imageUrls);
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        // ImageView for displaying images in a slider
        ViewPager2 itemImageSlider;
        TextView itemNameTextView, itemPriceTextView, itemDescriptionTextView;

        public MenuViewHolder(View itemView) {
            super(itemView);
            itemImageSlider = itemView.findViewById(R.id.itemImageSlider);  // RecyclerView for images
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
        }
    }

    // Method to display image slider in each item
    private void displayImageSlider(ViewPager2 itemImageSlider, List<String> imageUrls) {
        // Create an adapter for the image slider RecyclerView
        ImageAdapter imageSliderAdapter = new ImageAdapter(context, imageUrls);
        itemImageSlider.setAdapter(imageSliderAdapter);
    }
}
