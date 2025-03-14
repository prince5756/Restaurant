package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.user_restaurant.R;

public class Subscription extends AppCompatActivity {

    private ConstraintLayout planMonthly, planYearly, planPermanent, planCustom;
    private RadioButton radioMonthly, radioYearly, radioPermanent, radioCustom;
    private Drawable defaultBg;
    private Button continueBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription);

        // Initialize ConstraintLayouts
        planMonthly = findViewById(R.id.constraintLayout7);
        planYearly = findViewById(R.id.constraintLayout8);
        planPermanent = findViewById(R.id.constraintLayout9);
        planCustom = findViewById(R.id.constraintLayout10); // 4th option

        // Initialize RadioButtons
        radioMonthly = findViewById(R.id.radioButton1);
        radioYearly = findViewById(R.id.radioButton2);
        radioPermanent = findViewById(R.id.radioButton3);
        radioCustom = findViewById(R.id.radioButton4); // 4th radio

        continueBtn = findViewById(R.id.button5);

        // Save Default Background
        defaultBg = planMonthly.getBackground();

        // Click listeners for each plan
        planMonthly.setOnClickListener(view -> selectPlan(planMonthly, radioMonthly));
        planYearly.setOnClickListener(view -> selectPlan(planYearly, radioYearly));
        planPermanent.setOnClickListener(view -> selectPlan(planPermanent, radioPermanent));
        planCustom.setOnClickListener(view -> selectPlan(planCustom, radioCustom));

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueBtn.animate()
                        .scaleX(0.9f) // Slightly shrink the card
                        .scaleY(0.9f)
                        .alpha(0.8f)  // Slightly fade out
                        .setDuration(500) // Duration of the first animation phase
                        .withEndAction(() -> {
                            continueBtn.animate()
                                    .scaleX(1f) // Return to original size
                                    .scaleY(1f)
                                    .alpha(1f) // Return to full visibility
                                    .setDuration(500)
                                    .start();
                        })
                        .start();

                Intent intent = new Intent(Subscription.this, PaymentMethods.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void selectPlan(ConstraintLayout selectedLayout, RadioButton selectedRadio) {
        // Reset all to default
        resetSelection();

        // Set selected background
        selectedLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.black_box));

        // Change radio button tint
        selectedRadio.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.black));

        // Change radio button text color
        selectedRadio.setTextColor(Color.BLACK);

        selectedLayout.animate()
                .scaleX(0.9f) // Slightly shrink the card
                .scaleY(0.9f)
                .alpha(0.8f)  // Slightly fade out
                .setDuration(500) // Duration of the first animation phase
                .withEndAction(() -> {
                    selectedLayout.animate()
                            .scaleX(1f) // Return to original size
                            .scaleY(1f)
                            .alpha(1f) // Return to full visibility
                            .setDuration(500)
                            .start();
                })
                .start();

        // Set checked
        selectedRadio.setChecked(true);
    }

    private void resetSelection() {
        // Reset all layouts to default
        planMonthly.setBackground(defaultBg);
        planYearly.setBackground(defaultBg);
        planPermanent.setBackground(defaultBg);
        planCustom.setBackground(defaultBg);

        // Reset radio buttons
        radioMonthly.setChecked(false);
        radioYearly.setChecked(false);
        radioPermanent.setChecked(false);
        radioCustom.setChecked(false);

        // Reset radio button tint to default
        radioMonthly.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        radioYearly.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        radioPermanent.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        radioCustom.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        // Reset radio button text color to default (gray)
        radioMonthly.setTextColor(Color.GRAY);
        radioYearly.setTextColor(Color.GRAY);
        radioPermanent.setTextColor(Color.GRAY);
        radioCustom.setTextColor(Color.GRAY);
    }
}