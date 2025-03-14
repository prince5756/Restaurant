package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.user_restaurant.R;

public class PaymentMethods extends AppCompatActivity {

    private ConstraintLayout layoutCard, layoutNetBanking, layoutUPI;
    private ConstraintLayout childCard, childNetBanking, childUPI;
    private TextView textCard, textNetBanking, textUPI;
    private Drawable upArrow, defaultArrow;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_methods);

        // Initialize ConstraintLayouts
        layoutCard = findViewById(R.id.constraintLayout11);
        layoutNetBanking = findViewById(R.id.constraintLayout12);
        layoutUPI = findViewById(R.id.layout_upi);

        // Initialize Child ConstraintLayouts (Initially Gone)
        childCard = findViewById(R.id.child_layout_card);
        childNetBanking = findViewById(R.id.child_layout_net_banking);
        childUPI = findViewById(R.id.child_layout_upi);

        // Initialize TextViews
        textCard = findViewById(R.id.textView63);
        textNetBanking = findViewById(R.id.text_net_banking);
        textUPI = findViewById(R.id.text_upi);

        // Get drawables
        upArrow = ContextCompat.getDrawable(this, R.drawable.up_arrow); // Your up arrow drawable
        defaultArrow = ContextCompat.getDrawable(this, R.drawable.arrow); // Default right arrow drawable

        // Set click listeners
        layoutCard.setOnClickListener(view -> updateUI(textCard, childCard));
        layoutNetBanking.setOnClickListener(view -> updateUI(textNetBanking, childNetBanking));
        layoutUPI.setOnClickListener(view -> updateUI(textUPI, childUPI));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateUI(TextView selectedTextView, ConstraintLayout selectedChildLayout) {
        // Reset all TextViews
        resetTextViews();

        // Set selected TextView color and drawable
        selectedTextView.setTextColor(Color.BLACK);
        selectedTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, upArrow, null);

        // Show the selected child layout
        selectedChildLayout.setVisibility(View.VISIBLE);
    }

    private void resetTextViews() {
        // Reset to default state
        textCard.setTextColor(Color.GRAY);
        textCard.setCompoundDrawablesWithIntrinsicBounds(null, null, defaultArrow, null);
        childCard.setVisibility(View.GONE);

        textNetBanking.setTextColor(Color.GRAY);
        textNetBanking.setCompoundDrawablesWithIntrinsicBounds(null, null, defaultArrow, null);
        childNetBanking.setVisibility(View.GONE);

        textUPI.setTextColor(Color.GRAY);
        textUPI.setCompoundDrawablesWithIntrinsicBounds(null, null, defaultArrow, null);
        childUPI.setVisibility(View.GONE);
    }
}