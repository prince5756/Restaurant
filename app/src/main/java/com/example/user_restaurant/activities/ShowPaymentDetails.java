package com.example.user_restaurant.activities;

import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.user_restaurant.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowPaymentDetails extends AppCompatActivity {
    TextView txtPaymentAmount,txtPaymentDate,txtPaymentValidity,txtPaymentStatus;
    Button btnUpgrade;
    FirebaseFirestore db;
    String restaurantId;
    FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_payment_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        restaurantId = auth.getCurrentUser().getUid();

        txtPaymentAmount = findViewById(R.id.txtPaymentAmount);
        txtPaymentDate = findViewById(R.id.txtPaymentDate);
        txtPaymentValidity = findViewById(R.id.txtPaymentValidity);
        txtPaymentStatus = findViewById(R.id.txtPaymentStatus);
        btnUpgrade = findViewById(R.id.btnUpgrade);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fetchPaymentData();
        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchPaymentData();
        });

        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (getApplicationContext(),Subscription.class));
                finish();
            }
        });
    }

    private void fetchPaymentData() {
        swipeRefreshLayout.setRefreshing(true); // Start showing refresh icon
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    swipeRefreshLayout.setRefreshing(false); // Stop refresh icon
                    if(documentSnapshot.exists()) {
                        // Extract payment data
                        String amount = documentSnapshot.getString("paymentAmount");
                        String date = documentSnapshot.getString("paymentDate");
                        String validity = documentSnapshot.getString("paymentValidity");
                        String status = documentSnapshot.getString("paymentStatus");

                        // Update UI with proper null checks
                        txtPaymentAmount.setText(String.format("Payment Amount :"+"₹%s", (amount != null) ? amount : "00.00"));
                        txtPaymentDate.setText("Payment Date: " + ((date != null) ? date : "N/A"));
                        txtPaymentValidity.setText("Payment Validity: " + ((validity != null) ? validity : "N/A"));
                        txtPaymentStatus.setText("Payment Status: " + ((status != null) ? status : "pending"));
                    } else {
                        Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false); // Stop refresh icon on error
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error: ", e);
                });
    }
    }
