package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.fragments.Feedback;
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
    private LinearLayout emaildropdown,dropdownContent;
    ImageView imageView;
    TextView profilename,txtEmail,txtAddress;
    private Uri profileImageUri;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button backbtn = findViewById(R.id.BackBtn);
        Button logout = findViewById(R.id.logoutbtn); //logout btn
        auth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");


        Button setUpBtn = findViewById(R.id.setupbtn); //set up Button
        Button feedBackBtn = findViewById(R.id.feedback);// FeedBack Button
        Button historyBtn = findViewById(R.id.history); // History Button
        Button addressbtn = findViewById(R.id.addressbtn);
        Button emailbtn = findViewById(R.id.emailbtn);
        dropdownContent = findViewById(R.id.dropdown_content);
        emaildropdown = findViewById(R.id.email_dropdown);
        imageView = findViewById(R.id.imageView2);
        profilename = findViewById(R.id.profilename);
        txtEmail = findViewById(R.id.txtEmail);
        txtAddress = findViewById(R.id.txtAddress);

        //Load Button Animation
        Animation pullRight = AnimationUtils.loadAnimation(this, R.anim.button_pull_right);
        loadUserData();
        //Profile Back Button
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(profile.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //address dropdown
        addressbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (dropdownContent.getVisibility() == view.GONE){
//                    dropdownContent.setVisibility(View.VISIBLE);
//                    addressbtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up_arrow, 0);
//                }else {
//                    dropdownContent.setVisibility(View.GONE);
//                    addressbtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow, 0);
//                }
                toggleAddressContent();
            }

        });

        //email dropdown
        emailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (emaildropdown.getVisibility() == View.GONE){
//                    emaildropdown.setVisibility(View.VISIBLE);
//                    emailbtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up_arrow, 0);
//                }else {
//                    emaildropdown.setVisibility(View.GONE);
//                    emailbtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow, 0);
//                }

                toggleEmailContent();
            }
        });

        // Go To Feedback Activity
        feedBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent feedback = new Intent(profile.this, Feedback.class);
                startActivity(feedback);
                view.startAnimation(pullRight);
            }
        });

        //Go To History Activity
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent history = new Intent(profile.this, ActivityUpdateMenuItemDetails.class);
                startActivity(history);
                view.startAnimation(pullRight);
                finish();
            }
        });

        //Go To SetUp Activity
        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Logout Confirmation");
                builder.setMessage("Are you sure you want to log out?");

                // Set the positive button for "Yes"
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform the logout action
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        auth.signOut();
                        logout.animate()
                                .scaleX(0.9f) // Slightly shrink the card
                                .scaleY(0.9f)
                                .alpha(0.8f)  // Slightly fade out
                                .setDuration(500) // Duration of the first animation phase
                                .withEndAction(() -> {
                                    logout.animate()
                                            .scaleX(1f) // Return to original size
                                            .scaleY(1f)
                                            .alpha(1f) // Return to full visibility
                                            .setDuration(500)
                                            .start();
                                })
                                .start();

                        finish(); // End the activity
                    }
                });

                // Set the negative button for "No"
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });

                // Show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

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
        fadeIn.setDuration(500); // 500ms animation duration
        view.startAnimation(fadeIn);
    }

    private void fadeOutAnimation(View view) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500); // 500ms animation duration
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
                    String firstImageUrl = imageUrls.get(0); // Get the 0th index image
                    Glide.with(this)
                            .load(firstImageUrl)
                            .error(R.drawable.profile_pic) // Fallback image
                            .placeholder(R.drawable.profile_pic)
                            .into(imageView); // Assuming profilePicture is your ImageView
                }
//                inputMobile.setText(documentSnapshot.getString("mobile"));
//                // inputEmail.setText(documentSnapshot.getString("email"));
//                inputDob.setText(documentSnapshot.getString("dob"));
//                selectedGender = documentSnapshot.getString("gender");

                // Set gender in spinner
//                if (selectedGender != null) {
//                    int spinnerPosition = ((ArrayAdapter<String>) genderSpinner.getAdapter()).getPosition(selectedGender);
//                    genderSpinner.setSelection(spinnerPosition);
//                }

                // Load profile image
                String imageUrl = documentSnapshot.getString("profileImageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .error(R.drawable.profile_pic)
                            .placeholder(R.drawable.profile_pic)
                            .into(imageView);
                }
            } else {
                Glide.with(this).load(R.drawable.profile_pic).into(imageView);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}