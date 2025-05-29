package com.example.user_restaurant.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.user_restaurant.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComplaintActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ChipGroup chipGroupIssues;
    private TextInputEditText etDetails;
    private MaterialButton btnAttachScreenshot, btnSubmitComplaint;
    private ImageView btnBack, ivScreenshotPreview,imgComplaintHistory;

    private Uri selectedImageUri;
    private Cloudinary cloudinary;
    String userId,complaintId;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ProgressDialog to show while uploading or submitting
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // Initialize Cloudinary
        initCloudinary();

        // Initialize Views
        chipGroupIssues = findViewById(R.id.chipGroupIssues);
        etDetails = findViewById(R.id.etDetails);
        btnAttachScreenshot = findViewById(R.id.btnAttachScreenshot);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);
        btnBack = findViewById(R.id.btnBack);
        ivScreenshotPreview = findViewById(R.id.ivScreenshotPreview);
        imgComplaintHistory = findViewById(R.id.imgComplaintHistory);

        // ProgressDialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting your complaint...");
        progressDialog.setCancelable(false);

        // Back button
        btnBack.setOnClickListener(view -> finish());

        // Attach screenshot button
        btnAttachScreenshot.setOnClickListener(view -> openImagePicker());

        // Submit complaint button
        btnSubmitComplaint.setOnClickListener(view -> submitComplaint());
        imgComplaintHistory.setOnClickListener(v -> showPopupMenu(v));
    }

    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcltdegl9");
        config.put("api_key", "725369446425795");
        config.put("api_secret", "A_gnqylLW633-njiJSwd3mdZSso");
        cloudinary = new Cloudinary(config);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Show preview with Glide
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(ivScreenshotPreview);
            ivScreenshotPreview.setVisibility(View.VISIBLE);
        }
    }

    private void submitComplaint() {
        // Collect selected issues
        List<String> selectedIssues = new ArrayList<>();
        for (int i = 0; i < chipGroupIssues.getChildCount(); i++) {
            View child = chipGroupIssues.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selectedIssues.add(chip.getText().toString());
                }
            }
        }

        // Get description text
        String details = (etDetails.getText() != null)
                ? etDetails.getText().toString().trim()
                : "";

        // Validation 1: must have either an issue selected or a non-empty description
        if (selectedIssues.isEmpty() && details.isEmpty()) {
            Toast.makeText(this,
                    "Please select an issue or describe the problem",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation 2: if issues are selected, ensure at least 20 characters in details
        if (details.length() < 20 && !selectedIssues.isEmpty()) {
            Toast.makeText(this,
                    "Please provide at least 20 characters for the problem description",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation 3: screenshot must be attached (adjust logic as needed)
        if (selectedImageUri == null) {
            Toast.makeText(this,
                    "Please attach a screenshot to proceed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "User not authenticated! Please login again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        progressDialog.show();

        complaintId = db.collection("users")
                .document(userId).collection("Complaints")
                .document().getId();


        // Build complaint data
        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", currentUser.getUid());
        complaint.put("complaintId", complaintId);
        complaint.put("issues", selectedIssues);
        complaint.put("details", details);
        complaint.put("dateOfComplaintRegistered", FieldValue.serverTimestamp());
        complaint.put("status", "Pending");

        // If screenshot is attached, upload to Cloudinary first
        uploadImageToCloudinary(currentUser.getUid(), complaint);
    }

    private void uploadImageToCloudinary(String userId, Map<String, Object> complaint) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri)) {
                Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap(
                        "folder", "complaints",
                        "public_id", userId + "_" + System.currentTimeMillis()
                ));

                String imageUrl = (String) uploadResult.get("secure_url");
                complaint.put("screenshotUrl", imageUrl);

                // Now save to Firestore on the main thread
                runOnUiThread(() -> saveComplaintToFirestore(complaint));

            } catch (IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveComplaintToFirestore(Map<String, Object> complaint) {
        db.collection("users")
                .document(userId).collection("Complaints")
                .document(complaintId)
                .set(complaint)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Complaint submitted successfully!",
                            Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Error submitting complaint: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    private void showPopupMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
        popupMenu.inflate(R.menu.complaint_menu);
//        popupMenu.setForceShowIcon(true); // Show icons with menu items

        // Set menu item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_view_history) {
                // Handle view history click
                startActivity(new Intent(this, ComplaintHistoryActivity.class));
                return true;
            } else if (id == R.id.action_settings) {
                // Handle settings click
                return true;
            }
            return false;
        });

        // Show the popup menu
        if (!isFinishing()) {
            popupMenu.show();
        }
    }
    private void resetForm() {
        chipGroupIssues.clearCheck();
        etDetails.setText("");
        ivScreenshotPreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }
}
