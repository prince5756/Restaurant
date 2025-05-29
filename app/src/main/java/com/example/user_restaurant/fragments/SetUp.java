package com.example.user_restaurant.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.user_restaurant.R;
import com.example.user_restaurant.activities.AdditionInfoPro;
import com.example.user_restaurant.adapters.SetUpPageAdapter;
import com.example.user_restaurant.activities.Subscription;
import com.google.android.material.tabs.TabLayout;

public class SetUp extends AppCompatActivity {

    //subscribtion
    private boolean isSubscribed = false;
    Button btnSubscribe;

    TabLayout setuptabLayout;
    ViewPager2 setupviewPager2;
    SetUpPageAdapter setupPagerAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_up);

        //subscribe button
        btnSubscribe = findViewById(R.id.button2);

        setuptabLayout = findViewById(R.id.tabLayout2);
        setupviewPager2 = findViewById(R.id.viewPager2);

        // Run animation automatically after 1 second
        new Handler().postDelayed(() -> animateButtonWidth(dpToPx(130)), 1000);

        // Change the icon color to black and resize it
        Drawable[] drawables = btnSubscribe.getCompoundDrawables();
        Drawable leftDrawable = drawables[0]; // Left icon

        if (leftDrawable != null) {

            // Convert 15dp to pixels
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()
            );

            // Set icon size
            leftDrawable.setBounds(0, 0, sizeInPx, sizeInPx);

            // Apply the updated drawable
            btnSubscribe.setCompoundDrawables(leftDrawable, null, null, null);

            btnSubscribe.setOnClickListener(v -> {

                Intent intent = new Intent(SetUp.this, Subscription.class);
                startActivity(intent);

                if (!isSubscribed) {
                    applyButtonStyles();
                    if (leftDrawable != null) {
                        // Change icon color to black
                        leftDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);


                        // Apply the updated drawable
                        btnSubscribe.setCompoundDrawables(leftDrawable, null, null, null);
                    }
                    isSubscribed = true;
                }
            });


            setupPagerAdapter = new SetUpPageAdapter(this);
            setupviewPager2.setAdapter(setupPagerAdapter);

            setuptabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    View tabview = tab.view;
                    tabview.setBackgroundResource(R.drawable.tab_curved_bg);
                    setupviewPager2.setVisibility(View.VISIBLE);
                    setupviewPager2.setCurrentItem(tab.getPosition());
                    setuptabLayout.setTabTextColors(
                            getResources().getColor(R.color.unselected_txt_color),
                            getResources().getColor(R.color.selected_txt_color)
                    );
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    View tabview = tab.view;
                    tabview.setBackgroundResource(R.drawable.tab_curved_removebg);

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    setupviewPager2.setVisibility(View.VISIBLE);
                }

            });

            setupviewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    setuptabLayout.selectTab(setuptabLayout.getTabAt(position));
                }
            });

            setupviewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                        case 0:
                        case 1:
                        case 2:
                            setuptabLayout.getTabAt(position).select();
                    }
                    super.onPageSelected(position);
                }
            });

            TabLayout.Tab firstTab = setuptabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select(); // Programmatically select the first tab
                // Trigger background and text color change manually
                View firstTabView = firstTab.view;
                firstTabView.setBackgroundResource(R.drawable.tab_curved_bg);
                setuptabLayout.setTabTextColors(
                        getResources().getColor(R.color.unselected_txt_color),
                        getResources().getColor(R.color.selected_txt_color)
                );
            }


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

    }

    private void animateButtonWidth ( int endWidth){
        int startWidth = btnSubscribe.getWidth();

        ValueAnimator animator = ValueAnimator.ofInt(startWidth, endWidth);
        animator.setDuration(1000); // 1-second slow-motion effect
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            btnSubscribe.getLayoutParams().width = animatedValue;
            btnSubscribe.requestLayout();
        });

        animator.start();

        // Set text after animation ends
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                btnSubscribe.setText("Subscribe");
            }
        });
    }

    private int dpToPx ( int dp){
        TypedValue TypedValue = null;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void applyButtonStyles() {

        // Change text color to black
        btnSubscribe.setTextColor(Color.BLACK);

        // Change background color to white
        btnSubscribe.setBackgroundColor(Color.WHITE);
    }
}
