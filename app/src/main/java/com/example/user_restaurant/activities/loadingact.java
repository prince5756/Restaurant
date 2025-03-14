package com.example.user_restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;


public class loadingact extends AppCompatActivity {
    private static final int SPLASH_SCREEN_DURATION = 2500;
    private static final int LOAD_INTERVAL = 300;

    private TextView txtLoading;
    private Handler handler = new Handler();
    private int dotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loadingact);

        txtLoading = findViewById(R.id.txtLoading);

        // Start the loading animation
        handler.post(runnable);

        // Navigate to the next activity after the splash screen duration
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(loadingact.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_DURATION);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            dotCount = (dotCount + 1) % 4; // Cycle through 0, 1, 2, 3
            StringBuilder loadingText = new StringBuilder("Loading");
            for (int i = 0; i < dotCount; i++) {
                loadingText.append(" .");
            }
            txtLoading.setText(loadingText.toString());

            // Repeat this runnable code block every LOAD_INTERVAL milliseconds
            handler.postDelayed(this, LOAD_INTERVAL);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Stop the handler when the activity is destroyed
    }
}
