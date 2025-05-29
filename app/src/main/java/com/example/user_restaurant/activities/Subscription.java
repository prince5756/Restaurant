package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.user_restaurant.R;
import com.google.firebase.auth.FirebaseAuth;

public class Subscription extends AppCompatActivity {
    FirebaseAuth auth;
    String restaurantUid;
    private ConstraintLayout planMonthly, planYearly, planPermanent, planCustom;
    private RadioButton radioMonthly, radioYearly, radioPermanent, radio5Per;
    private Drawable defaultBgMonthly, defaultBgYearly, defaultBgPermanent, defaultBgCustom;
    private Button continueBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription);

        auth = FirebaseAuth.getInstance();
        restaurantUid = auth.getCurrentUser().getUid();

        // Initialize ConstraintLayouts
        planMonthly = findViewById(R.id.constraintLayout7);
        planYearly = findViewById(R.id.constraintLayout8);
        planPermanent = findViewById(R.id.constraintLayout9);
        planCustom = findViewById(R.id.constraintLayout10);

        // Initialize RadioButtons
        radioMonthly = findViewById(R.id.radioButton1);
        radioYearly = findViewById(R.id.radioButton2);
        radioPermanent = findViewById(R.id.radioButton3);
        radio5Per = findViewById(R.id.radioButton4);

        continueBtn = findViewById(R.id.button5);

        // Save Default Backgrounds
        defaultBgMonthly = planMonthly.getBackground();
        defaultBgYearly = planYearly.getBackground();
        defaultBgPermanent = planPermanent.getBackground();
        defaultBgCustom = planCustom.getBackground();

        // Click listeners for each plan
        planMonthly.setOnClickListener(view -> selectPlan(planMonthly, radioMonthly));
        planYearly.setOnClickListener(view -> selectPlan(planYearly, radioYearly));
        planPermanent.setOnClickListener(view -> selectPlan(planPermanent, radioPermanent));
        planCustom.setOnClickListener(view -> selectPlan(planCustom, radio5Per));

        continueBtn.setOnClickListener(view -> {
            animateButton(continueBtn);

            Intent intent = new Intent(Subscription.this, PaymentMethods.class);
            intent.putExtra("restaurantUid", restaurantUid);
            if (radioMonthly.isChecked()) {
                intent.putExtra("amount", " 599.00");
            } else if (radioYearly.isChecked()) {
                intent.putExtra("amount", " 5499.00");
            } else if (radioPermanent.isChecked()) {
                intent.putExtra("amount", " 25999.00");
            } else if (radio5Per.isChecked()) {
                intent.putExtra("amount", " 100.00");
            }
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void selectPlan(ConstraintLayout selectedLayout, RadioButton selectedRadio) {
        resetSelection();

        // Apply new styles
        selectedLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.black_box));
        selectedRadio.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.black));
        selectedRadio.setTextColor(Color.BLACK);

        animateSelection(selectedLayout);
        selectedRadio.setChecked(true);
    }

    private void resetSelection() {
        // Reset layouts
        planMonthly.setBackground(defaultBgMonthly);
        planYearly.setBackground(defaultBgYearly);
        planPermanent.setBackground(defaultBgPermanent);
        planCustom.setBackground(defaultBgCustom);

        // Reset radio buttons
        resetRadioButton(radioMonthly);
        resetRadioButton(radioYearly);
        resetRadioButton(radioPermanent);
        resetRadioButton(radio5Per);
    }

    private void resetRadioButton(RadioButton radioButton) {
        radioButton.setChecked(false);
        radioButton.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        radioButton.setTextColor(Color.GRAY);
    }

    private void animateSelection(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .alpha(0.9f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start())
                .start();
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .alpha(0.9f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start())
                .start();
    }
}
