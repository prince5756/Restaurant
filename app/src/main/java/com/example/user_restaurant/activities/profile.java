package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.AlphaAnimation;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.example.user_restaurant.ContactUs;
import com.example.user_restaurant.fragments.History;
import com.example.user_restaurant.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class profile extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    public boolean clicked;
    private StorageReference storageRef;
    private boolean isEmailContentVisible = false;
    private boolean isAddressContentVisible = false;
    private LinearLayout emaildropdown, dropdownContent;
    ImageView imageView;
    TextView profilename, txtEmail, txtAddress;
    Button btnPaymentDetails,btnCheckRequest,btnComplaint,btnAddEmployeeDetails, btnContactUs;
    private Uri profileImageUri;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button backbtn = findViewById(R.id.BackBtn);
        Button logout = findViewById(R.id.logoutbtn); // logout btn
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        Button setUpBtn = findViewById(R.id.setupbtn); // set up Button
        Button feedBackBtn = findViewById(R.id.feedback); // FeedBack Button
        Button setholidaybtn = findViewById(R.id.setholidaybtn); // FeedBack Button
    // History Button
        Button addressbtn = findViewById(R.id.addressbtn);
        Button emailbtn = findViewById(R.id.emailbtn);
        // Rating Button from XML
//        Button rattingbtn = findViewById(R.id.rattingbtn);
//        btnSetting = findViewById(R.id.btnSetting);
        dropdownContent = findViewById(R.id.dropdown_content);
        emaildropdown = findViewById(R.id.email_dropdown);
        imageView = findViewById(R.id.imageView2);
        profilename = findViewById(R.id.profilename);
        txtEmail = findViewById(R.id.txtEmail);
        txtAddress = findViewById(R.id.txtAddress);
        btnPaymentDetails=findViewById(R.id.btnPaymentDetails);
        btnCheckRequest=findViewById(R.id.btnCheckRequest);
        btnComplaint=findViewById(R.id.btnComplaint);
        btnAddEmployeeDetails=findViewById(R.id.btnAddEmployeeDetails);
        btnContactUs = findViewById(R.id.contactbtn);

        //Contact Us Button
        btnContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ContactUs.class));
                //   finish();
            }
        });

        // Load Button Animation
        Animation pullRight = AnimationUtils.loadAnimation(this, R.anim.button_pull_right);
        loadUserData();

        btnCheckRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ActivityCheckRegistrationRequest.class));
               // finish();
            }
        });
        btnPaymentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ShowPaymentDetails.class));
             //   finish();
            }
        });

        setholidaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CloseRestaurantActivity.class));
               // finish();
            }
        });
        btnComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ComplaintActivity.class));
               // finish();
            }
        });
        btnAddEmployeeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),StaffDetailsActivity.class));

            }
        });
        // Profile Back Button
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(profile.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // address dropdown
        addressbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddressContent();
            }
        });

        // email dropdown
        emailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEmailContent();
            }
        });

        // Go To Feedback Activity
        feedBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent feedback = new Intent(profile.this, FeedbackActivity.class);
                startActivity(feedback);
                view.startAnimation(pullRight);
            }
        });

        // Go To History Activity
//        historyBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent history = new Intent(profile.this, ActivityUpdateMenuItemDetails.class);
//                startActivity(history);
//                view.startAnimation(pullRight);
//                finish();
//            }
//        });

        // Go To SetUp Activity
        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TermsAndCondition.class));
                //finish();
                // Set up activity code here if needed.
            }
        });

        // Logout Button with AlertDialog confirmation
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Logout Confirmation");
                builder.setMessage("Are you sure you want to log out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        auth.signOut();
                        logout.animate()
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .alpha(0.8f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    logout.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .alpha(1f)
                                            .setDuration(500)
                                            .start();
                                })
                                .start();
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // ★ Dynamic Rating Update Code in Profile Activity ★
        // When the rating button is clicked, show a dialog with a RatingBar.
//        rattingbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // Create a RatingBar programmatically
//                final RatingBar ratingBar = new RatingBar(profile.this);
//                ratingBar.setNumStars(5);
//                ratingBar.setStepSize(0.5f);  // Allows half-star ratings
//                ratingBar.setRating(0);
//
//                // Optionally, you can wrap the RatingBar in a container (here, a LinearLayout)
//                LinearLayout container = new LinearLayout(profile.this);
//                container.setOrientation(LinearLayout.VERTICAL);
//                container.setPadding(50, 20, 50, 20);
//                container.addView(ratingBar, new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(profile.this);
//                builder.setTitle("Rate Us");
//                builder.setMessage("Please rate your experience out of 5 stars:");
//                builder.setView(container);
//                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        float ratingValue = ratingBar.getRating();
//                        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
//                        if (currentUserId == null) {
//                            Toast.makeText(profile.this, "User not logged in", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        // Store the dynamic rating value in Firestore
//                        db.collection("RestaurantInformation")
//                                .document(currentUserId)
//                                .update("rating", String.valueOf(ratingValue))
//                                .addOnSuccessListener(aVoid ->
//                                        Toast.makeText(profile.this, "Rating updated: " + ratingValue, Toast.LENGTH_SHORT).show())
//                                .addOnFailureListener(e ->
//                                        Toast.makeText(profile.this, "Failed to update rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                    }
//                });
//                builder.setNegativeButton("Cancel", null);
//                builder.create().show();
//            }
//        });
        // ★ End Dynamic Rating Update Code ★
    }

    private void toggleEmailContent() {
        if (isEmailContentVisible) {
            fadeOutAnimation(emaildropdown);
        } else {
            fadeInAnimation(emaildropdown);
        }
        isEmailContentVisible = !isEmailContentVisible;
    }

    private void toggleAddressContent() {
        if (isAddressContentVisible) {
            fadeOutAnimation(dropdownContent);
        } else {
            fadeInAnimation(dropdownContent);
        }
        isAddressContentVisible = !isAddressContentVisible;
    }

    private void fadeInAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        view.startAnimation(fadeIn);
    }

    private void fadeOutAnimation(View view) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new AlphaAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });
        view.startAnimation(fadeOut);
    }

    private void loadUserData() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("RestaurantInformation").document(currentUserId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                profilename.setText(documentSnapshot.getString("restaurantName"));
                txtEmail.setText(documentSnapshot.getString("email"));
                txtAddress.setText(documentSnapshot.getString("location"));

                List<String> imageUrls = (List<String>) documentSnapshot.get("restaurantImageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String firstImageUrl = imageUrls.get(0);
                    Glide.with(this)
                            .load(firstImageUrl)
                            .error(R.drawable.profile_pic)
                            .placeholder(R.drawable.profile_pic)
                            .into(imageView);
                }
                // Optionally load additional user data here
            } else {
                Glide.with(this).load(R.drawable.profile_pic).into(imageView);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
