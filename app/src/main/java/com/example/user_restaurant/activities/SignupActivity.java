package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameField, emailField, phoneField, createPasswordField, confirmPasswordField;
    private CheckBox privacyCheckbox;
    private Button btnSignup;
    private ProgressBar progressBar;
    private boolean isPasswordVisibleCreate = false;
    private boolean isPasswordVisibleConfirm = false;
    private ImageView createShowPasswordIcon, confirmPasswordIcon;
    FirebaseAuth mAuth;
    Boolean isApproved = false;
    Boolean isPayment = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    FirebaseFirestore firestore;
    StorageReference storageReference,imageName;
    String userId;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
//
//        TextView btnSignUp = findViewById(R.id.btnSignUp);
//        btnSignUp.setOnClickListener(v -> {
//            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish(); // Close SignupActivity
//        });


        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar=findViewById(R.id.progressBar);
        usernameField = findViewById(R.id.userField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        createPasswordField = findViewById(R.id.createPasswordField);
        confirmPasswordField = findViewById(R.id.passwordField);
        privacyCheckbox = findViewById(R.id.privacyCheckbox);
        btnSignup = findViewById(R.id.btnLogin);
        createShowPasswordIcon = findViewById(R.id.createShowPasswordIcon);
        confirmPasswordIcon = findViewById(R.id.ConfirmPasswordIcon);

        // Initially disable the Signup button
        //  btnSignup.setEnabled(false);
        btnSignup.setAlpha(0.5f);

        // Add TextWatchers
        usernameField.addTextChangedListener(signupTextWatcher);
        emailField.addTextChangedListener(signupTextWatcher);
        phoneField.addTextChangedListener(signupTextWatcher);
        createPasswordField.addTextChangedListener(signupTextWatcher);
        confirmPasswordField.addTextChangedListener(signupTextWatcher);
        privacyCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> validateFields());

        // Show/Hide password functionality
        createShowPasswordIcon.setOnClickListener(v -> togglePasswordVisibility(createPasswordField, createShowPasswordIcon, true));
        confirmPasswordIcon.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordField, confirmPasswordIcon, false));

        // Agree Privacy Policy
        TextView privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());

        TextView termsConditions = findViewById(R.id.termsConditions);
        termsConditions.setOnClickListener(v -> showTermsAndConditionsDialog());

        // Button Signup Click
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View view) {
                btnSignup.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                String strName = usernameField.getText().toString();
                String strEmail = emailField.getText().toString();
                String strPhone = phoneField.getText().toString();
                String strPass = createPasswordField.getText().toString();
                String strConf = confirmPasswordField.getText().toString();

                if(!validateFields()){

                    btnSignup.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
                Date now = new Date();
                String filename;
                filename = format.format(now);

                //if (!validateFields()) {

                mAuth.createUserWithEmailAndPassword(strEmail, strPass)
                        .addOnCompleteListener(task -> {
                            // showProgress(true);
                            if (task.isSuccessful()) {

                                btnSignup.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);

                                setContentView(R.layout.activity_loadingact);
                                new Handler().postDelayed(() -> {
                                    SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("RestaurantInformation", true); // Store login state
                                    editor.apply(); // Apply changes
                                }, 1800);
                                loadUserData();
                                String userName = strName;
                                SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userName", userName);
                                editor.apply();

                                Toast.makeText(SignupActivity.this, "Welcome " + userName, Toast.LENGTH_SHORT).show();

                                //  startActivity(new Intent(SignupActivity.this, MainActivity.class));

                                //    finish();

                                //  HelperProfileData helperProfileData = new HelperProfileData(strName, strEmail, strPass);
                                storageReference = FirebaseStorage.getInstance().getReference(filename);
                                imageName = storageReference;

                                userId = mAuth.getCurrentUser().getUid();
                                String restaurantId = db.collection("RestaurantInformation").document().getId();

                                DocumentReference documentReference = firestore.collection("RestaurantInformation").document(userId);

                                Map<String, Object> userData = new HashMap<>();

//                                if(profileUri == null){
                                userData.put("IsUserRestaurant","1");
                                userData.put("restaurantName", strName);
                                userData.put("email", strEmail);
                                userData.put("phone", strPhone);
                                userData.put("password", strPass);
                                userData.put("userId", userId);
                                userData.put("restaurantId",restaurantId);
                                userData.put("isApproved",isApproved);
                                userData.put("isPayment",isPayment);
                                userData.put("paymentStatus","pending");
                                userData.put("totalNumberOfSeats","0");
                                userData.put("signupDate", new Date());
                                userData.put("isClosed", false);

                                firestore.collection("RestaurantInformation")
                                        .document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Toast.makeText(SignupActivity.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                                                navigateToHome();
                                            }
                                        });


//                                } else {
//                                    storageReference.putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                @Override
//                                                public void onSuccess(Uri uri) {
//                                                    showProgress(true);
//                                                    String strUri = uri.toString();
//
//                                                    userData.put("name", strName);
//                                                    userData.put("email", strEmail);
//                                                    userData.put("phone", strPhone);
//                                                    userData.put("password", strPass);
//                                                    userData.put("userId", userId);
//                                                    userData.put("profileUri", strUri);
//                                                    userData.put("isUser","1");
//
//
//                                                    firestore.collection("users")
//                                                            .document(userId)
//                                                            .set(userData)
//                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void unused) {
//
//                                                                    Toast.makeText(SignupActivity.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
//                                                                 //   navigateToHome();
//                                                                }
//                                                            });
//
//                                                }
//                                            });
//
//                                        }
//                                    });
//                                }
//
                            } else {

                                btnSignup.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                //showProgress(false);
                                handleRegistrationError(task.getException());
                            }
                        });

//
//                } else {
//                    Toast.makeText(SignupActivity.this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        btnSignup.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }


    // TextWatcher for validating fields
    private final TextWatcher signupTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateFields();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    private boolean validateFields() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String createPassword = createPasswordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        boolean isValid = true;

        if (username.isEmpty()) {
            usernameField.setError("Username cannot be empty!");
            isValid = false;
        }

        if (!email.contains("@")) {
            emailField.setError("Invalid email format!");
            isValid = false;
        }

        if (phone.length() != 10) {
            phoneField.setError("Phone number must be 10 digits!");
            isValid = false;
        }

        if (createPassword.length() < 8) {
            createPasswordField.setError("Password must be at least 8 characters long!");
            isValid = false;
        }

        if (!createPassword.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match!");
            isValid = false;
        }

        if (!privacyCheckbox.isChecked()) {
            isValid = false;
        }

        btnSignup.setEnabled(isValid);
        btnSignup.setAlpha(isValid ? 1.0f : 0.5f);

        return isValid;
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon, boolean isCreatePassword) {
        if (isCreatePassword ? isPasswordVisibleCreate : isPasswordVisibleConfirm) {
            passwordField.setInputType(129); // Password hidden
            toggleIcon.setImageResource(R.drawable.eye_closed);
        } else {
            passwordField.setInputType(1); // Password visible
            toggleIcon.setImageResource(R.drawable.eye_open);
        }
        passwordField.setSelection(passwordField.getText().length());
        if (isCreatePassword) {
            isPasswordVisibleCreate = !isPasswordVisibleCreate;
        } else {
            isPasswordVisibleConfirm = !isPasswordVisibleConfirm;
        }
    }

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

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy");
        builder.setMessage("This is the privacy policy for this application. Your data is secure and private.");
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void handleRegistrationError(Exception exception) {
        if (exception != null) {
            Toast.makeText(SignupActivity.this,"User has already register ", Toast.LENGTH_SHORT).show();
        }
    }
    private void navigateToHome() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
    public void redirectToLogin(View view){
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}
