package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private TextView forgotPassword;
    private ImageView showPasswordIcon;
    private Button btnLogin;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    private FirebaseFirestore fStore;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);  // Set the status bar color to transparent
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

//        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        forgotPassword = findViewById(R.id.forgotPassword);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        showPasswordIcon = findViewById(R.id.showPasswordIcon);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar=findViewById(R.id.progressBar);
        TextView privacyPolicy = findViewById(R.id.privacyPolicy);
        TextView termsConditions = findViewById(R.id.termsConditions);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signUpTextView = findViewById(R.id.btnSignUp); // Sign Up TextView

        // Add text watchers to validate fields
//        emailField.addTextChangedListener(loginTextWatcher);
//        passwordField.addTextChangedListener(loginTextWatcher);

        // Show/Hide password functionality
        showPasswordIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Navigate to SignUpActivity when "Sign Up" text is clicked
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Terms & Conditions dialog
        termsConditions.setOnClickListener(v -> showTermsAndConditionsDialog());

        // Privacy Policy dialog
        privacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());

        // Forgot Password functionality
        forgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password? Generate the link here.", Toast.LENGTH_SHORT).show();
        });

        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLogin.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                //   Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                if (validateInput(email, password)) {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                String userId = auth.getCurrentUser().getUid();
                                DocumentReference docRef = fStore.collection("RestaurantInformation").document(userId);
                                docRef.get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.getString("IsUserRestaurant") != null) {
                                                btnLogin.setVisibility(View.GONE);
                                                progressBar.setVisibility(View.VISIBLE);

                                                setContentView(R.layout.activity_loadingact);
                                                new Handler().postDelayed(() -> {
                                                    SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putBoolean("isLoggedIn", true); // Store login state
                                                    editor.apply(); // Apply changes

                                                }, 1800);
                                                loadUserData();
                                                startActivity(new Intent(LoginActivity.this, ActivityFragmentManager.class));
                                                SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                                String userName = sharedPref.getString("userName", "User");
                                                Toast.makeText(LoginActivity.this, "Welcome " + userName, Toast.LENGTH_SHORT).show();

                                                finish();


                                            } else {
                                                btnLogin.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                                handleLoginFailure();
                                            }
                                        })
                                        .addOnFailureListener(e -> handleLoginFailure());
                            })
                            .addOnFailureListener(e -> handleLoginFailure());
                } else {
                    //   progressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Login button click


//            if (validateFields()) {
//                Toast.makeText(this, "Login Successfully!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show();
//            }
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError(TextUtils.isEmpty(email) ? "Enter email" : "Email is invalid");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Enter password");
            return false;
        }
        return true;
    }
    private void handleLoginFailure() {
        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        // progressBar.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
    }
    // TextWatcher to enable/disable login button
//    private final TextWatcher loginTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            validateFields();
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) { }
//    };
//
//    // Validate fields and enable/disable login button
//    private boolean validateFields() {
//
//        boolean isValid = true;
//
//        if (email.isEmpty()) {
//            emailField.setError("Email must not be empty!");
//            isValid = false;
//        }
//
//        if (password.length() < 8) {
//            passwordField.setError("Password must be at least 8 characters long!");
//            isValid = false;
//        }
//
//        btnLogin.setEnabled(isValid);
//        btnLogin.setAlpha(isValid ? 1.0f : 0.5f);
//
//        return isValid;
//    }

    // Toggle password visibility
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setInputType(129); // Password hidden
            showPasswordIcon.setImageResource(R.drawable.eye_closed);
        } else {
            passwordField.setInputType(1); // Password visible
            showPasswordIcon.setImageResource(R.drawable.eye_open);
        }
        passwordField.setSelection(passwordField.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    // Show Terms & Conditions Dialog
    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions");
        builder.setMessage("Here are the terms and conditions. \n\n" +
                "1. You agree to not misuse the app.\n" +
                "2. Your data is secure.\n" +
                "3. Follow all rules and regulations.\n\n" +
                "Thank you for understanding!");
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Show Privacy Policy Dialog
    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy");
        builder.setMessage("This is the privacy policy for this application. Your data is secure and private.");
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void loadUserData() {
        String currentUserId = auth.getCurrentUser().getUid();


        // Reference to Firestore
        DocumentReference userRef = db.collection("RestaurantInformation").document(currentUserId);

        // Fetch user data
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Extract data
                String name = documentSnapshot.getString("restaurantName");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");
                String profileUri = documentSnapshot.getString("profileUri");


                String userName = name;
                SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userName", userName);
                editor.apply();




            }
        }).addOnFailureListener(e -> {

        });
    }
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
        EditText emailBox = dialogView.findViewById(R.id.emailBox);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btnReset).setOnClickListener(view -> resetPassword(emailBox));
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }
    private void resetPassword(EditText emailBox) {
        String userEmail = emailBox.getText().toString().trim();
        if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {

            Toast.makeText(LoginActivity.this, "Enter a valid email id", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            db.collection("RestaurantInformation").whereEqualTo("email",userEmail).get().
                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    auth.sendPasswordResetEmail(userEmail)
                                            .addOnCompleteListener(sentEmailTask -> {
                                                if (sentEmailTask.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
