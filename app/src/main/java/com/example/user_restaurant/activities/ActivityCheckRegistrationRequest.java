package com.example.user_restaurant.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.ImageSliderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActivityCheckRegistrationRequest extends AppCompatActivity {
    private ViewPager2 viewPagerImages;
    private TextView textValidity, textPaymentAmount, textPaymentStatus, textPaymentDate,
            textRestaurantName, textLocation, textItemName, textRestaurantId, textUserId,
            textPhone, textEmail, textTotalSeats,textNote,textRequest;
    private Button buttonDeny, buttonApproved;

    private FirebaseFirestore db;
    private String restaurantId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_regeistration_request);

        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        setupFirestore();
        fetchRestaurantData();

     //   setupButtons();
    }

    private void initializeViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        textValidity = findViewById(R.id.textValidity);
        textPaymentAmount = findViewById(R.id.textPaymentAmount);
        textPaymentStatus = findViewById(R.id.textPaymentStatus);
        textPaymentDate = findViewById(R.id.textPaymentDate);
        textRestaurantName = findViewById(R.id.textRestaurantName);
        textLocation = findViewById(R.id.textLocation);
        textNote = findViewById(R.id.textNote);
        textRequest = findViewById(R.id.textRequest);
  //      textItemName = findViewById(R.id.textItemName);
        textRestaurantId = findViewById(R.id.textRestaurantId);
        textUserId = findViewById(R.id.textUserId);
        textPhone = findViewById(R.id.textPhone);
        textEmail = findViewById(R.id.textEmail);
        textTotalSeats = findViewById(R.id.textTotalSeats);
      //  buttonDeny = findViewById(R.id.buttonDeny);
      //  buttonApproved = findViewById(R.id.buttonApproved);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        restaurantId = auth.getCurrentUser().getUid();
        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, "Invalid restaurant ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchRestaurantData() {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateUI(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });
    }

    private void populateUI(DocumentSnapshot document) {

       // Payment Information
        textPaymentAmount.setText(String.format("₹%s", document.getString("paymentAmount")));
        textPaymentDate.setText(document.getString("paymentDate"));
        textValidity.setText(document.getString("paymentValidity"));
        textPaymentStatus.setText(document.getString("paymentStatus"));

        // Restaurant Details
        textRestaurantName.setText(document.getString("restaurantName"));
        textPhone.setText(document.getString("phone"));
        textEmail.setText(document.getString("email"));
        textRestaurantId.setText(document.getString("restaurantId"));
        textLocation.setText(document.getString("location"));
        textTotalSeats.setText(document.getString("totalNumberOfSeats"));
        textUserId.setText(document.getString("userId"));

        // Status Flags
        Boolean isPayment = document.getBoolean("isPayment");
        Boolean isApproved = document.getBoolean("isApproved");

        String validity = document.getString("paymentValidity");

        if(isApproved==false || isPayment==false){
            textNote.setText("Your restaurant will appear on the user app when the payment status is "+"paid " +"and the request is " + "approved."+" Once you have successfully made the payment, both fields will be updated.");
        } else {
            textNote.setText("Your Restaurant shows on user app till the " + validity);

        }

        if(isApproved==true){
            textRequest.setText("Approved");
        }else {
            textRequest.setText("pending");
        }

        List<String> imageUrls = new ArrayList<>();
        if (document.contains("restaurantImageUrls")) {
            try {
                imageUrls = (List<String>) document.get("restaurantImageUrls");
            } catch (Exception e) {
                Log.e("ImageLoad", "Error getting image URLs", e);
            }
        }

        if (!imageUrls.isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageUrls);
            viewPagerImages.setAdapter(adapter);
        }
    }
//
//    private void setupButtons() {
//        buttonApproved.setOnClickListener(v -> approveRestaurant());
//        buttonDeny.setOnClickListener(v -> denyRestaurant());
//    }

    private void approveRestaurant() {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .update("isApproved", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Restaurant approved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Approval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Approval error: ", e);
                });
    }

    private void denyRestaurant() {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Restaurant registration denied", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Denial failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Deletion error: ", e);
                });
    }
}