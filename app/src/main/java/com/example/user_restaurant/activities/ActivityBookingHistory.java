package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityBookingHistory extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView txtName, txtEmail, txtPhone, txtCustomerId, txtBookingId,
            txtDate, txtTime, txtGuest, txtRestaurantId;
    String expiryTimestamp; // if needed

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtCustomerId = findViewById(R.id.txtCustomerId);
        txtBookingId = findViewById(R.id.txtBookingId);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        txtGuest = findViewById(R.id.txtGuest);
        txtRestaurantId = findViewById(R.id.txtRestaurantId);

        // Retrieve data passed via intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String bookingId = extras.getString("bookingId");
            String restaurantId = extras.getString("restaurantId");
            String customerId = extras.getString("customerId");
            String date = extras.getString("date");
            String time = extras.getString("time");
            String mealType = extras.getString("mealType");
            String guestCount = extras.getString("guestCount");
            String bookingTimestamp = extras.getString("bookingTimestamp");
            expiryTimestamp = extras.getString("expiryTimestamp");

            // Current user profile details passed from AdapterPndOdr
            String name = extras.getString("name");
            String phone = extras.getString("phone");
            String email = extras.getString("email");
            String profileImageUrl = extras.getString("profileImageUrl");

            txtName.setText(name != null ? name : "N/A");
            txtPhone.setText(phone != null ? phone : "N/A");
            txtEmail.setText(email != null ? email : "N/A");
            txtCustomerId.setText(customerId != null ? customerId : "N/A");
            txtBookingId.setText(bookingId != null ? bookingId : "N/A");
            txtDate.setText(date != null ? date : "N/A");
            txtTime.setText(time != null ? time : "N/A");
            txtGuest.setText(guestCount != null ? guestCount : "N/A");
            txtRestaurantId.setText(restaurantId != null ? restaurantId : "N/A");

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(profileImageUrl))
                        .placeholder(R.drawable.utveek)
                        .into(imgProfile);
            }
        }
    }
}

    /**
     * Helper method to parse a Firestore Timestamp string into milliseconds.
     */

