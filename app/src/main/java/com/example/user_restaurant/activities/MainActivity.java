package com.example.user_restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.user_restaurant.R;
import com.example.user_restaurant.fragments.SetUp;
import com.example.user_restaurant.fragments.bookfragment;
import com.example.user_restaurant.fragments.favfragment;
import com.example.user_restaurant.fragments.homefragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    public FloatingActionButton setUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomview);
        frameLayout = findViewById(R.id.framlayout);

        // Load the default fragment (HomeFragment) initially
        loadFragment(new homefragment());

        setUpBtn = findViewById(R.id.setUpBtn);
        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SetUp.class);
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.navhome) {
                    selectedFragment = new homefragment();
                } else if (itemId == R.id.navfav) {
                    selectedFragment = new favfragment();
                } else if (itemId == R.id.navbook) {
                    selectedFragment = new bookfragment();
                } else if (itemId == R.id.navprofile) {
                    Intent intent = new Intent(MainActivity.this, profile.class);
                    startActivity(intent);
                    return true;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
                return true;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framlayout, fragment);
        fragmentTransaction.commit();
    }
}
