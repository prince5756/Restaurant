package com.example.user_restaurant;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;

public class FragmentOrdersHistory extends Fragment {

    ImageView imgProfile;
    TextView txtName, txtEmail, txtPhone, txtCustomerId, txtBookingId,
            txtDate, txtTime, txtGuest, txtRestaurantId;
    Button cancleBtn, conformBtn;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_his_odr_detail, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtCustomerId = view.findViewById(R.id.txtCustomerId);
        txtBookingId = view.findViewById(R.id.txtBookingId);
        txtDate = view.findViewById(R.id.txtDate);
        txtTime = view.findViewById(R.id.txtTime);
        txtGuest = view.findViewById(R.id.txtGuest);
        txtRestaurantId = view.findViewById(R.id.txtRestaurantId);
        cancleBtn = view.findViewById(R.id.button3);
        conformBtn = view.findViewById(R.id.button4);

        if (getArguments() != null){
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

            txtName.setText(name);
            txtPhone.setText(phone);
            txtEmail.setText(email);
            txtCustomerId.setText(customerId);
            txtBookingId.setText(bookingId);
            txtDate.setText(date);
            txtTime.setText(time);
            txtGuest.setText(guestCount);
            txtRestaurantId.setText(restaurantId);
        }

        cancleBtn.setOnClickListener(view1 -> {
            cancleBtn.animate().scaleX(0.9f).scaleY(0.9f).alpha(0.8f)
                    .setDuration(500)
                    .withEndAction(() -> cancleBtn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).start())
                    .start();
        });

        conformBtn.setOnClickListener(view12 -> {
            conformBtn.animate().scaleX(0.9f).scaleY(0.9f).alpha(0.8f)
                    .setDuration(500)
                    .withEndAction(() -> conformBtn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).start())
                    .start();
        });

        return view;
    }
}
