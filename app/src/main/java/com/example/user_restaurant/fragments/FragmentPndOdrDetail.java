package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;

public class FragmentPndOdrDetail extends Fragment {

    ImageView imgProfile;
    TextView txtStatus, txtName, txtEmail, txtPhone, txtCustomerId, txtBookingId,
            txtDate, txtTime, txtGuest, txtRestaurantId;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pnd_odr_detail, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtName);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtCustomerId = view.findViewById(R.id.txtCustomerId);
        txtBookingId = view.findViewById(R.id.txtBookingId);
        txtDate = view.findViewById(R.id.txtDate);
        txtTime = view.findViewById(R.id.txtTime);
        txtGuest = view.findViewById(R.id.txtGuest);
        txtRestaurantId = view.findViewById(R.id.txtRestaurantId);

        if (getArguments() != null) {
            String Status = getArguments().getString("Status");
            String name = getArguments().getString("name");
            String phone = getArguments().getString("phone");
            String bookingId = getArguments().getString("bookingId");
            String restaurantId = getArguments().getString("restaurantId");
            String customerId = getArguments().getString("customerId");
            String date = getArguments().getString("date");
            String time = getArguments().getString("time");
            String email = getArguments().getString("email");
            String mealType = getArguments().getString("mealType");
            String guestCount = getArguments().getString("guestCount");
            String bookingTimestamp = getArguments().getString("bookingTimestamp");
            String expiryTimestamp = getArguments().getString("expiryTimestamp");
            String profileImageUrl = getArguments().getString("profileImageUrl");

            txtName.setText(name != null ? name : "N/A");
            txtStatus.setText(Status != null ? Status : "N/A");
            txtPhone.setText(phone != null ? phone : "N/A");
            txtEmail.setText(email != null ? email : "N/A");
            txtCustomerId.setText(customerId != null ? customerId : "N/A");
            txtBookingId.setText(bookingId != null ? bookingId : "N/A");
            txtDate.setText(date != null ? date : "N/A");
            txtTime.setText(time != null ? time : "N/A");
            txtGuest.setText(guestCount != null ? guestCount : "N/A");
            txtRestaurantId.setText(restaurantId != null ? restaurantId : "N/A");

            // Set txtStatus color based on the Status value
            if (Status != null) {
                if (Status.equalsIgnoreCase("Cancelled")) {
                    txtStatus.setTextColor(Color.RED);
                } else if (Status.equalsIgnoreCase("Booked")) {
                    txtStatus.setTextColor(Color.GREEN);
                } else if (Status.equalsIgnoreCase("Schedule Updated")) {
                    txtStatus.setTextColor(Color.BLUE);
                }
            }

            // Load image with safe handling
            if (profileImageUrl != null &&
                    !profileImageUrl.trim().isEmpty() &&
                    !profileImageUrl.equalsIgnoreCase("null")) {
                try {
                    Uri imageUri = Uri.parse(profileImageUrl);
                    Glide.with(this)
                            .load(imageUri)
                            .placeholder(R.drawable.profile_tab)
                            .into(imgProfile);
                } catch (Exception e) {
                    Log.e("FragmentPndOdrDetail", "Error loading image: " + e.getMessage());
                    imgProfile.setImageResource(R.drawable.profile_tab);
                }
            } else {
                // Use default placeholder image if URL is invalid
                imgProfile.setImageResource(R.drawable.profile_tab);
            }
        }

        return view;
    }
}
