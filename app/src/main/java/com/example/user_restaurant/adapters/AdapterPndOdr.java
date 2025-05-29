package com.example.user_restaurant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.user_restaurant.fragments.FragmentPndOdrDetail;
import com.example.user_restaurant.R;
import com.example.user_restaurant.models.ModelBooking;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterPndOdr extends RecyclerView.Adapter<AdapterPndOdr.ViewHolder> {
    private Context context;
    private List<ModelBooking> bookingList;
    private FirebaseFirestore db;

    // Cache customer details (keyed by customerId)
    private Map<String, CustomerData> customerCache = new HashMap<>();

    public AdapterPndOdr(Context context, List<ModelBooking> bookingList) {
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
        holder.txtStatus.setText(booking.getStatus());
        holder.txtDate.setText(booking.getBookingDate() != null ? booking.getBookingDate().toString() : "N/A");
        holder.imageView.setImageResource(R.drawable.profile_tab);

        if (booking.getStatus() != null) {
            if (booking.getStatus().equalsIgnoreCase("Cancelled")) {
                holder.txtStatus.setTextColor(Color.RED);
            } else if (booking.getStatus().equalsIgnoreCase("Booked")) {
                holder.txtStatus.setTextColor(Color.GREEN);
            } else if (booking.getStatus().equalsIgnoreCase("Schedule Updated")) {
                holder.txtStatus.setTextColor(Color.BLUE);
            }
        }

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

        // Handle item click to show booking details
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
            bundle.putString("Status", booking.getStatus());
            FragmentPndOdrDetail fragment = new FragmentPndOdrDetail();
            fragment.setArguments(bundle);

            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
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

            // Additional check to ensure profileImageUrl isn't null, empty, or a "null" string
            if (customer.profileImageUrl != null &&
                    !customer.profileImageUrl.trim().isEmpty() &&
                    !customer.profileImageUrl.equalsIgnoreCase("null")) {
                try {
                    Glide.with(context)
                            .load(customer.profileImageUrl)
                            .placeholder(R.drawable.profile_tab)
                            .into(holder.imageView);
                } catch (Exception e) {
                    // Log the error and set the default placeholder image if Glide fails
                    Log.e("Glide Error", "Error loading image: " + e.getMessage());
                    holder.imageView.setImageResource(R.drawable.profile_tab);
                }
            } else {
                holder.imageView.setImageResource(R.drawable.profile_tab);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtName, txtCustomerId, txtDate, txtStatus;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView18);
            txtName = itemView.findViewById(R.id.txtName);
            txtCustomerId = itemView.findViewById(R.id.txtCustomerId);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
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
