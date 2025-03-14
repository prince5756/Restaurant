package com.example.user_restaurant.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;
//import com.example.user_restaurant.activities.ActivityBookingHistory;
import com.example.user_restaurant.activities.ActivityBookingHistory;
import com.example.user_restaurant.models.ModelBooking;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterTotalNumberOfBookings extends RecyclerView.Adapter<AdapterTotalNumberOfBookings.ViewHolder> {
    private Context context;
    private List<ModelBooking> bookingList;
    private FirebaseFirestore db;

    // Cache customer details (keyed by customerId)
    private Map<String, CustomerData> customerCache = new HashMap<>();

    public AdapterTotalNumberOfBookings(Context context, List<ModelBooking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_pnd_odr, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelBooking booking = bookingList.get(position);

        // Set default placeholders until customer data is loaded
        holder.txtName.setText("Loading...");
        holder.txtCustomerId.setText("Loading...");
        holder.txtDate.setText(booking.getBookingDate() != null ? booking.getBookingDate().toString() : "N/A");
        holder.imageView.setImageResource(R.drawable.profile_tab);

        // Check cache for customer data using booking's customerId
        if (customerCache.containsKey(booking.getCustomerId())) {
            CustomerData customer = customerCache.get(booking.getCustomerId());
            updateHolder(holder, customer);
        } else {
            // Fetch customer details from "users" collection
            db.collection("users").document(booking.getCustomerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            CustomerData customer = new CustomerData(
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("phone"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("profileImageUrl")
                            );
                            customerCache.put(booking.getCustomerId(), customer);
                            updateHolder(holder, customer);
                        } else {
                            holder.txtName.setText("Unknown");
                            holder.txtCustomerId.setText("Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdapterPndOdr", "Error fetching customer data: " + e.getMessage());
                        holder.txtName.setText("Error");
                        holder.txtCustomerId.setText("Error");
                    });
        }

        // Handle item click to launch ActivityBookingHistory
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            // Use fetched customer details if available
            CustomerData customer = customerCache.get(booking.getCustomerId());
            if (customer != null) {
                bundle.putString("name", customer.name);
                bundle.putString("phone", customer.phone);
                bundle.putString("email", customer.email);
                bundle.putString("profileImageUrl", customer.profileImageUrl);
            } else {
                bundle.putString("name", "");
                bundle.putString("phone", "");
                bundle.putString("email", "");
                bundle.putString("profileImageUrl", "");
            }
            // Pass booking-specific data
            bundle.putString("bookingId", booking.getBookingId());
            bundle.putString("restaurantId", booking.getRestaurantId());
            bundle.putString("customerId", booking.getCustomerId());
            bundle.putString("date", booking.getDate());
            bundle.putString("time", booking.getTime());
            bundle.putString("mealType", booking.getMealType());
            bundle.putString("guestCount", String.valueOf(booking.getGuestCount()));
            bundle.putString("bookingTimestamp", String.valueOf(booking.getBookingTimestamp()));
            bundle.putString("expiryTimestamp", String.valueOf(booking.getExpiryTimestamp()));

            // Create an intent to start ActivityBookingHistory and pass the bundle
            Intent intent = new Intent(context, ActivityBookingHistory.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private void updateHolder(ViewHolder holder, CustomerData customer) {
        if (customer != null) {
            holder.txtName.setText(customer.name != null ? customer.name : "N/A");
            holder.txtCustomerId.setText(customer.phone != null ? customer.phone : "N/A");
            if (customer.profileImageUrl != null && !customer.profileImageUrl.isEmpty()) {
                Glide.with(context)
                        .load(customer.profileImageUrl)
                        .placeholder(R.drawable.profile_tab)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.profile_tab);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtName, txtCustomerId, txtDate;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView18);
            txtName = itemView.findViewById(R.id.txtName);
            txtCustomerId = itemView.findViewById(R.id.txtCustomerId);
            txtDate = itemView.findViewById(R.id.txtDate);
            cardView = itemView.findViewById(R.id.cardView3);
        }
    }

    // Container class for customer data
    private static class CustomerData {
        String name;
        String phone;
        String email;
        String profileImageUrl;

        CustomerData(String name, String phone, String email, String profileImageUrl) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.profileImageUrl = profileImageUrl;
        }
    }
}
