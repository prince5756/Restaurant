package com.example.user_restaurant.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user_restaurant.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AddressTab extends Fragment {
    // UI Elements
    private RadioGroup wifiRadioGroup, parkingRadioGroup;
    private CheckBox vegCheckbox, nonVegCheckbox, veganCheckbox, jainCheckbox;
    private EditText discountEditText;
    private Button submitButton;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String restaurantId; // Get this from intent or SharedPreferences

    // For update mode
    private boolean isUpdateMode = false;
    private String additionalInfoId = null; // Will store the document id if exists

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_tab, container, false);

        auth = FirebaseAuth.getInstance();

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        restaurantId = auth.getCurrentUser().getUid(); // Replace with actual ID retrieval logic

        // Initialize UI elements
        wifiRadioGroup = view.findViewById(R.id.radioGroup); // Assign correct ID from XML
        parkingRadioGroup = view.findViewById(R.id.radioGroup2); // Assign correct ID from XML
        vegCheckbox = view.findViewById(R.id.checkBox);
        nonVegCheckbox = view.findViewById(R.id.checkBox2);
        veganCheckbox = view.findViewById(R.id.checkBox3);
        jainCheckbox = view.findViewById(R.id.checkBox4);
        discountEditText = view.findViewById(R.id.editTextDiscountOffer);
        submitButton = view.findViewById(R.id.setupformbtn);

        // Check if AdditionalInformation exists
        fetchExistingAdditionalInfo();

        submitButton.setOnClickListener(v -> saveAdditionalInfo());

        return view;
    }

    private void fetchExistingAdditionalInfo() {
        // Query for existing AdditionalInformation document.
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .collection("AdditionalInformation")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Document exists; switch to update mode and pre-fill fields.
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        additionalInfoId = doc.getId();
                        isUpdateMode = true;
                        submitButton.setText("Update");

                        // Pre-fill UI fields using fetched data
                        String wifi = doc.getString("wifi");
                        String parking = doc.getString("parking");
                        List<String> mealCategories = (List<String>) doc.get("mealCategories");
                        Long discount = doc.getLong("discount");

                        // Set radio buttons for wifi
                        setRadioGroupSelection(wifiRadioGroup, wifi);
                        // Set radio buttons for parking
                        setRadioGroupSelection(parkingRadioGroup, parking);

                        // Set checkboxes for mealCategories
                        if (mealCategories != null) {
                            if (mealCategories.contains(vegCheckbox.getText().toString())) {
                                vegCheckbox.setChecked(true);
                            }
                            if (mealCategories.contains(nonVegCheckbox.getText().toString())) {
                                nonVegCheckbox.setChecked(true);
                            }
                            if (mealCategories.contains(veganCheckbox.getText().toString())) {
                                veganCheckbox.setChecked(true);
                            }
                            if (mealCategories.contains(jainCheckbox.getText().toString())) {
                                jainCheckbox.setChecked(true);
                            }
                        }
                        // Set discount if exists
                        if (discount != null) {
                            discountEditText.setText(String.valueOf(discount));
                        }
                    }
                });
    }

    // Helper method to set radio group selection based on text value.
    private void setRadioGroupSelection(RadioGroup radioGroup, String value) {
        if (value == null || value.isEmpty()) return;
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View v = radioGroup.getChildAt(i);
            if (v instanceof RadioButton) {
                RadioButton rb = (RadioButton) v;
                if (rb.getText().toString().equalsIgnoreCase(value)) {
                    rb.setChecked(true);
                    break;
                }
            }
        }
    }

    private void saveAdditionalInfo() {
        // Get data from views
        String wifi = getSelectedRadioValue(wifiRadioGroup);
        String parking = getSelectedRadioValue(parkingRadioGroup);
        List<String> mealCategories = getMealCategories();
        String discount = discountEditText.getText().toString().trim();

        // Validate inputs (example)
        if (wifi.isEmpty() || parking.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create data object
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("wifi", wifi);
        additionalInfo.put("parking", parking);
        additionalInfo.put("mealCategories", mealCategories);
        if (!discount.isEmpty()) {
            try {
                additionalInfo.put("discount", Integer.parseInt(discount));
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid discount value", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Add common fields
        String currentDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK).format(new Date());
        additionalInfo.put("date", currentDate);
        additionalInfo.put("userId", restaurantId);

        if (isUpdateMode && additionalInfoId != null) {
            // Also store the document id in the field "infoId" during update
            additionalInfo.put("infoId", additionalInfoId);
            // Update the existing document
            db.collection("RestaurantInformation")
                    .document(restaurantId)
                    .collection("AdditionalInformation")
                    .document(additionalInfoId)
                    .set(additionalInfo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Data updated successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add a new document
            DocumentReference docRef = db.collection("RestaurantInformation")
                    .document(restaurantId)
                    .collection("AdditionalInformation")
                    .document();
            // Store the document id as a field
            additionalInfo.put("infoId", docRef.getId());
            docRef.set(additionalInfo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Data saved successfully!", Toast.LENGTH_SHORT).show();
                        // Set update mode for future changes
                        isUpdateMode = true;
                        additionalInfoId = docRef.getId();
                        submitButton.setText("Update");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getSelectedRadioValue(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return ""; // No selection
        RadioButton radioButton = getView().findViewById(selectedId);
        return radioButton.getText().toString();
    }

    private List<String> getMealCategories() {
        List<String> categories = new ArrayList<>();
        if (vegCheckbox.isChecked()) categories.add(vegCheckbox.getText().toString());
        if (nonVegCheckbox.isChecked()) categories.add(nonVegCheckbox.getText().toString());
        if (veganCheckbox.isChecked()) categories.add(veganCheckbox.getText().toString());
        if (jainCheckbox.isChecked()) categories.add(jainCheckbox.getText().toString());
        return categories;
    }
}
