package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.user_restaurant.R;
import com.example.user_restaurant.fragments.bookfragment;
import com.example.user_restaurant.fragments.favfragment;
import com.example.user_restaurant.fragments.homefragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityFragmentManager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_manager); // Ensure this matches the XML layout filename

        // Setup Toolbar
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup BottomNavigationView
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(this::onBottomNavigationItemSelected);
        }

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new homefragment());
        }
    }

    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        if (item.getItemId() == R.id.navhome) {
            fragment = new homefragment();
        } else if (item.getItemId() == R.id.navfav) {
            fragment = new favfragment();
        } else if (item.getItemId() == R.id.navbook) {
            fragment = new bookfragment();
        } else if (item.getItemId() == R.id.navprofile) {
            startActivity(new Intent(getApplicationContext(), profile.class));
            finish();
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
