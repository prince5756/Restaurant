package com.example.user_restaurant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;
import com.example.user_restaurant.models.ModelMenu;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdapterMenuList extends RecyclerView.Adapter<AdapterMenuList.MenuViewHolder> {

    private Context context;
    private List<ModelMenu> menuList;
    private FirebaseFirestore db;

    public AdapterMenuList(Context context, List<ModelMenu> menuList) {
        this.context = context;
        this.menuList = menuList;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_pnd_odr, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        ModelMenu menuItem = menuList.get(position);

        holder.txtName.setText(menuItem.getItemName());
        holder.txtPrice.setText("₹" + menuItem.getItemPrice());
        holder.foodType.setText( menuItem.getFoodType());
     //   holder.mealType.setText("Meal: " + menuItem.getMealType());

        // Edit button click listener
//        holder.btnEdit.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ActivityUpdateMenuItemDetails.class);
//            intent.putExtra("itemId", menuItem.getItemId());
//            intent.putExtra("restaurantId", menuItem.getRestaurantId());
//            intent.putExtra("itemName", menuItem.getItemName());
//            intent.putExtra("itemPrice", menuItem.getItemPrice());
//            intent.putExtra("foodType", menuItem.getFoodType());
//            intent.putExtra("mealType", menuItem.getMealType());
//            intent.putExtra("itemDescription", menuItem.getItemDescription());
//            intent.putExtra("imageUrls", menuItem.getFoodImageUrls().toArray(new String[0]));
//            context.startActivity(intent);
//        });
        Glide.with(context).load(menuItem.getFoodImageUrls().get(0)).into(holder.imageView);

        // Load images in ViewPager2
//        if (menuItem.getFoodImageUrls() != null && !menuItem.getFoodImageUrls().isEmpty()) {
//            ImageAdapter imageSliderAdapter = new ImageAdapter(context, menuItem.getFoodImageUrls());
//            holder.imageView.setAdapter(imageSliderAdapter);
//        }

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
        ImageView imageView;
        TextView txtName, txtPrice, foodType;
        CardView cardView;


        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView18);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtCustomerId);
            foodType = itemView.findViewById(R.id.txtDate);
            cardView = itemView.findViewById(R.id.cardView3);
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
