package com.example.user_restaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.user_restaurant.R;
import com.example.user_restaurant.activities.ActivityUpdateMenuItemDetails;
import com.example.user_restaurant.models.ModelMenu;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdapterMenu extends RecyclerView.Adapter<AdapterMenu.MenuViewHolder> {

    private Context context;
    private List<ModelMenu> menuList;
    private FirebaseFirestore db;

    public AdapterMenu(Context context, List<ModelMenu> menuList) {
        this.context = context;
        this.menuList = menuList;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item_viewholder, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        ModelMenu menuItem = menuList.get(position);

        holder.itemName.setText(menuItem.getItemName());
        holder.itemPrice.setText("₹" + menuItem.getItemPrice());
        holder.foodType.setText("Type: " + menuItem.getFoodType());
        holder.mealType.setText("Meal: " + menuItem.getMealType());

        // Edit button click listener
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityUpdateMenuItemDetails.class);
            intent.putExtra("itemId", menuItem.getItemId());
            intent.putExtra("restaurantId", menuItem.getRestaurantId());
            intent.putExtra("itemName", menuItem.getItemName());
            intent.putExtra("itemPrice", menuItem.getItemPrice());
            intent.putExtra("foodType", menuItem.getFoodType());
            intent.putExtra("mealType", menuItem.getMealType());
            intent.putExtra("quantity", menuItem.getQuantity());
            intent.putExtra("itemDescription", menuItem.getItemDescription());
            intent.putExtra("imageUrls", menuItem.getFoodImageUrls().toArray(new String[0]));
            context.startActivity(intent);
        });

        // Load images in ViewPager2
        if (menuItem.getFoodImageUrls() != null && !menuItem.getFoodImageUrls().isEmpty()) {
            ImageAdapter imageSliderAdapter = new ImageAdapter(context, menuItem.getFoodImageUrls());
            holder.foodImage.setAdapter(imageSliderAdapter);
        }

        // Long press to delete
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(menuItem, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 foodImage;
        TextView itemName, itemPrice, foodType, mealType;
        Button btnEdit;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            foodType = itemView.findViewById(R.id.foodType);
            mealType = itemView.findViewById(R.id.mealType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    // Method to show delete confirmation dialog
    private void showDeleteConfirmationDialog(ModelMenu menuItem, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this menu item?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMenuItem(menuItem, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Method to delete menu item from Firestore
    private void deleteMenuItem(ModelMenu menuItem, int position) {
        db.collection("RestaurantInformation")
                .document(menuItem.getRestaurantId())
                .collection("Menu")
                .document(menuItem.getItemId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from list and notify RecyclerView
                    menuList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Menu item deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show());
    }
}
