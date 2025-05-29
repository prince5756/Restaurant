package com.example.user_restaurant.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar ratingBarFeedback;
    private TextInputEditText etFeedbackComments;
    private MaterialButton btnSubmitFeedback;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;
    private ProgressDialog progressDialog;
    private String feedbackId; // Added feedback ID field

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = auth.getCurrentUser().getUid();
        feedbackId = userId + "_feedback"; // Custom feedback ID format

        initializeViews();
        checkExistingFeedback();
    }

    private void initializeViews() {
        ratingBarFeedback = findViewById(R.id.ratingBarFeedback);
        etFeedbackComments = findViewById(R.id.etFeedbackComments);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void checkExistingFeedback() {
        showProgressDialog("Checking existing feedback...");

        db.collection("RestaurantInformation")
                .document(userId)
                .collection("feedBackForAdmin")
                .document(feedbackId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    dismissProgressDialog();
                    if (documentSnapshot.exists()) {
                        populateExistingFeedback(documentSnapshot);
                        btnSubmitFeedback.setText("Update Feedback");
                    }
                })
                .addOnFailureListener(e -> showError("Fetch error: " + e.getMessage()));
    }

    private void populateExistingFeedback(com.google.firebase.firestore.DocumentSnapshot document) {
        Float rating = document.getDouble("rating") != null ?
                document.getDouble("rating").floatValue() : 0;
        String comments = document.getString("comments");
        String date = document.getString("date");

        ratingBarFeedback.setRating(rating);
        if (comments != null) etFeedbackComments.setText(comments);
        if (date != null) {
            // If you need to display the stored date somewhere
            // txtDateDisplay.setText(date);
        }
    }

    private void submitFeedback() {
        float rating = ratingBarFeedback.getRating();
        String comments = etFeedbackComments.getText() != null ?
                etFeedbackComments.getText().toString().trim() : "";

        if (rating == 0 && comments.isEmpty()) {
            Toast.makeText(this, "Please provide rating or feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog(btnSubmitFeedback.getText().toString().contains("Update") ?
                "Updating..." : "Submitting...");

        Map<String, Object> feedback = createFeedbackData(rating, comments);

        db.collection("RestaurantInformation")
                .document(userId)
                .collection("feedBackForAdmin")
                .document(feedbackId)
                .set(feedback, SetOptions.merge())
                .addOnSuccessListener(aVoid -> handleSuccess())
                .addOnFailureListener(e -> showError("Submission failed: " + e.getMessage()));
    }

    private Map<String, Object> createFeedbackData(float rating, String comments) {
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("feedbackId", feedbackId);
        feedback.put("userId", userId);
        feedback.put("rating", rating);
        feedback.put("comments", comments);
        feedback.put("date", getCurrentDateTime());
        return feedback;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void handleSuccess() {
        dismissProgressDialog();
        String message = btnSubmitFeedback.getText().toString().contains("Update") ?
                "Feedback updated!" : "Thanks for your feedback!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        if (!btnSubmitFeedback.getText().toString().contains("Update")) {
            btnSubmitFeedback.setText("Update Feedback");
        }
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        dismissProgressDialog();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}