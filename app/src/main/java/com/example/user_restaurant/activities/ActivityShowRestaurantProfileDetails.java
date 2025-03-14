package com.example.user_restaurant.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.user_restaurant.R;
import com.example.user_restaurant.UserDoesNotLogIn;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityShowRestaurantProfileDetails extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextInputEditText inputName, inputMobile, inputEmail;
    private EditText editMapAddress;
    private Button btnFetchAddress;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ImageView selectImagesButton, profileCircleview;
    Button updateProfileButton;
    private List<Uri> profileImageUris = new ArrayList<>();
    private Cloudinary cloudinary;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_restaurant_profile_details);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        inputName = findViewById(R.id.inputName);
        inputMobile = findViewById(R.id.inputMobile);
        inputEmail = findViewById(R.id.inputEmail);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        profileCircleview = findViewById(R.id.profileCircleview);
        selectImagesButton = findViewById(R.id.editProfilePicture);
        editMapAddress = findViewById(R.id.editMapAddress);
        btnFetchAddress = findViewById(R.id.btnFetchAddress);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Fetch Address Button Logic
        btnFetchAddress.setOnClickListener(v -> fetchAddressFromLocation());

        initCloudinary();
        loadUserData();
        // Progress Dialog for showing upload progress
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Your Details...");
        progressDialog.setCancelable(false);

        inputEmail.setOnClickListener(v-> Toast.makeText(this, "You can not Change your email", Toast.LENGTH_SHORT).show());
        selectImagesButton.setOnClickListener(view -> openImageChooser());
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();

            }
        });

    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcltdegl9"); // Replace with your Cloudinary cloud name
        config.put("api_key", "725369446425795"); // Replace with your Cloudinary API key
        config.put("api_secret", "A_gnqylLW633-njiJSwd3mdZSso"); // Replace with your Cloudinary API secret
        cloudinary = new Cloudinary(config);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUris.clear();

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    profileImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) { // Single image selected
                profileImageUris.add(data.getData());
            }

            Toast.makeText(this, profileImageUris.size() + " images selected", Toast.LENGTH_SHORT).show();
        }
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
                inputName.setText(documentSnapshot.getString("restaurantName"));
                inputMobile.setText(documentSnapshot.getString("phone"));
                inputEmail.setText(documentSnapshot.getString("email"));
                editMapAddress.setText(documentSnapshot.getString("location"));

                // Fetch and display the first image from the list
                List<String> imageUrls = (List<String>) documentSnapshot.get("restaurantImageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String firstImageUrl = imageUrls.get(0); // Get the 0th index image
                    Glide.with(this)
                            .load(firstImageUrl)
                            .error(R.drawable.profile_pic) // Fallback image
                            .placeholder(R.drawable.profile_pic)
                            .into(profileCircleview); // Assuming profilePicture is your ImageView
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


    private void updateUserProfile() {
        String name = inputName.getText().toString().trim();
        String mobile = inputMobile.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String mapAddress = editMapAddress.getText().toString();

        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        updateProfileButton.setVisibility(View.GONE);

        Map<String, Object> userData = new HashMap<>();
        userData.put("restaurantName", name);
        userData.put("phone", mobile);
        userData.put("email", email);
        userData.put("location", mapAddress);

        if (!profileImageUris.isEmpty()) {
            uploadImagesToCloudinary(currentUserId, imageUrls -> {
                userData.put("restaurantImageUrls", imageUrls);
                saveUserDataToFirestore(currentUserId, userData);
            });
        } else {
            saveUserDataToFirestore(currentUserId, userData);
        }
    }

    private void uploadImagesToCloudinary(String userId, ImageUploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<String> uploadedImageUrls = new ArrayList<>();

        executor.execute(() -> {
            try {
                for (Uri imageUri : profileImageUris) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap(
                            "folder", "restaurantImages",
                            "public_id", userId + "_" + System.currentTimeMillis()
                    ));
                    uploadedImageUrls.add((String) uploadResult.get("secure_url"));
                }
                runOnUiThread(() -> callback.onImagesUploaded(uploadedImageUrls));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveUserDataToFirestore(String userId, Map<String, Object> userData) {
        db.collection("RestaurantInformation").document(userId)
                .update(userData)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Restaurant details updated successfully", Toast.LENGTH_SHORT).show();
                    updateProfileButton.setVisibility(View.VISIBLE);
                    //clearControls();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update your restaurant details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateProfileButton.setVisibility(View.VISIBLE);
                });
    }

    private interface ImageUploadCallback {
        void onImagesUploaded(List<String> imageUrls);
    }

    private void fetchAddressFromLocation() {
        // Check Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    if (location != null) {
                        // Use Geocoder to fetch the address
                        Geocoder geocoder = new Geocoder(ActivityShowRestaurantProfileDetails.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String fullAddress = address.getAddressLine(0);
                                editMapAddress.setText(fullAddress);
                                Toast.makeText(ActivityShowRestaurantProfileDetails.this, "Address fetched!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ActivityShowRestaurantProfileDetails.this, "Unable to fetch address!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityShowRestaurantProfileDetails.this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivityShowRestaurantProfileDetails.this, "Location is null, please check if GPS is enabled.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityShowRestaurantProfileDetails.this, "Unable to get location! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchAddressFromLocation(); // Retry fetching location after permission is granted
            } else {
                Toast.makeText(this, "Permission Denied! Cannot fetch address.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
//    public void clearControls(){
//
//        Glide.with(this).load(R.drawable.circle_shape).into(profileCircleview);
//        inputEmail.setText("");
//        inputName.setText("");
//        inputMobile.setText("");
//        editMapAddress.setText("");
//    }

