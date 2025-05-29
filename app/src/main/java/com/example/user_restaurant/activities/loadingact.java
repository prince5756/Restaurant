package com.example.user_restaurant.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.user_restaurant.R;

public class loadingact extends AppCompatActivity {
    private static final int SPLASH_SCREEN_DURATION = 2500;
    private static final int LOAD_INTERVAL = 300;

    private TextView txtLoading;
    private Handler handler = new Handler();
    private int dotCount = 0;
    private BroadcastReceiver networkReceiver;
    private boolean isConnected = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (txtLoading != null && !isFinishing()) { // Added activity state check
                dotCount = (dotCount + 1) % 4;
                StringBuilder loadingText = new StringBuilder("Loading");
                for (int i = 0; i < dotCount; i++) loadingText.append(" .");
                txtLoading.setText(loadingText.toString());
                handler.postDelayed(this, LOAD_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetworkConnection(); // Network check FIRST
    }

    private void checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) setupOnlineLayout();
        else setupOfflineLayout();
    }

    private void setupOnlineLayout() {
        setContentView(R.layout.activity_loadingact);
        txtLoading = findViewById(R.id.txtLoading);

        // Clear previous callbacks
        handler.removeCallbacksAndMessages(null);
        handler.post(runnable);

        new Handler().postDelayed(this::redirectBasedOnLogin, SPLASH_SCREEN_DURATION);
    }

    private void setupOfflineLayout() {
        setContentView(R.layout.activity_loadingact_offline);

        // Stop all handler operations
        handler.removeCallbacksAndMessages(null);
        txtLoading = null; // Prevent lingering references

        // Initialize fresh receiver
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isFinishing()) checkNetworkConnection();
            }
        };

        try {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void redirectBasedOnLogin() {
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);
        startActivity(new Intent(loadingact.this, isLoggedIn ? MainActivity.class : LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected && networkReceiver != null) {
            try {
                registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException ignored) {}
    }
}