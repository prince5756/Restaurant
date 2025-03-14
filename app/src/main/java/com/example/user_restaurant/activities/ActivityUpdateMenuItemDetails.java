package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.user_restaurant.R;
import com.example.user_restaurant.fragments.MenuTab;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityUpdateMenuItemDetails extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView itemImageView;
    private EditText edtItemName, edtItemPrice, edtItemDescription;
    private AutoCompleteTextView autoCompleteFoodType, autoCompleteMealType;
    private Button btnUpdate, selectImageButton,btnCancel;
    private ArrayList<Uri> selectedImageUris;
    private Cloudinary cloudinary;
    private boolean areImagesSelected = false;
    private FirebaseFirestore db;
    private String restaurantId, menuId;
    String[] imageUrls;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        final Dialog dialog = new Dialog(ActivityUpdateMenuItemDetails.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_bottomsheet_update_menu_details);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCanceledOnTouchOutside(false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        edtItemName = dialog.findViewById(R.id.edtItemName);
        edtItemPrice = dialog.findViewById(R.id.edtItemPrice);
        edtItemDescription = dialog.findViewById(R.id.editTextText3);
        autoCompleteFoodType = dialog.findViewById(R.id.autoCompleteTextView);
        autoCompleteMealType = dialog.findViewById(R.id.autoCompleteTextView2);
        itemImageView = dialog.findViewById(R.id.imageView5);
        btnUpdate = dialog.findViewById(R.id.btnUpdate);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        selectImageButton = dialog.findViewById(R.id.selectImgBtn);


        // Check if AutoCompleteTextView is found
        if (autoCompleteFoodType == null || autoCompleteMealType == null) {
            Toast.makeText(this, "AutoCompleteTextView not found!", Toast.LENGTH_LONG).show();
            return;
        }

        // Predefined Food Types
        List<String> foodTypesList = new ArrayList<>(Arrays.asList("Vegetarian", "Non-Vegetarian", "Vegan", "Gluten-Free"));
        ArrayAdapter<String> foodTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foodTypesList);
        autoCompleteFoodType.setAdapter(foodTypeAdapter);

        // Predefined Meal Types
        List<String> mealTypesList = new ArrayList<>(Arrays.asList("Breakfast", "Lunch", "Dinner", "Snacks"));
        ArrayAdapter<String> mealTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mealTypesList);
        autoCompleteMealType.setAdapter(mealTypeAdapter);

        // Get Data from Intent
        Intent intent = getIntent();
        restaurantId = intent.getStringExtra("restaurantId");
        menuId = intent.getStringExtra("itemId");

        String foodType = intent.getStringExtra("foodType");
        String mealType = intent.getStringExtra("mealType");

        // Ensure fetched values are in the dropdown list
        if (!foodTypesList.contains(foodType) && foodType != null) {
            foodTypesList.add(foodType);
            foodTypeAdapter.notifyDataSetChanged();
        }
        if (!mealTypesList.contains(mealType) && mealType != null) {
            mealTypesList.add(mealType);
            mealTypeAdapter.notifyDataSetChanged();
        }

        // Set existing values
        edtItemName.setText(intent.getStringExtra("itemName"));
        edtItemPrice.setText(intent.getStringExtra("itemPrice"));
        autoCompleteFoodType.setText(foodType, false);
        autoCompleteMealType.setText(mealType, false);
        edtItemDescription.setText(intent.getStringExtra("itemDescription"));
        imageUrls = getIntent().getStringArrayExtra("imageUrls");

        if (imageUrls != null && imageUrls.length > 0) {
            Glide.with(this).load(imageUrls[0]).into(itemImageView);
        }
        initCloudinary();

        btnCancel.setOnClickListener(v -> onBackPressed());
        // Update Button Click
        btnUpdate.setOnClickListener(v -> updateMenuItem());
        selectImageButton.setOnClickListener(v -> openImageChooser());

        // Select Image Button (Placeholder for Image Selection Logic)
    }
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Item Images"), PICK_IMAGE_REQUEST);
    }
    private void initCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dcltdegl9");
        config.put("api_key", "725369446425795");
        config.put("api_secret", "A_gnqylLW633-njiJSwd3mdZSso");
        cloudinary = new Cloudinary(config);
    }
    private void updateMenuItem() {
        String itemName = edtItemName.getText().toString().trim();
        String itemPrice = edtItemPrice.getText().toString().trim();
        String foodType = autoCompleteFoodType.getText().toString().trim();
        String mealType = autoCompleteMealType.getText().toString().trim();
        String itemDescription = edtItemDescription.getText().toString().trim();

        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemPrice) ||
                TextUtils.isEmpty(foodType) || TextUtils.isEmpty(mealType) || TextUtils.isEmpty(itemDescription)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating menu item...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("itemName", itemName);
        updateData.put("itemPrice", itemPrice);
        updateData.put("foodType", foodType);
        updateData.put("mealType", mealType);
        updateData.put("itemDescription", itemDescription);

        if (areImagesSelected) {
            uploadMultipleImagesToCloudinary(imageUrls -> {
                updateData.put("foodImageUrls", imageUrls);
               saveUserDataToFirestore(menuId, updateData);
            });
        }else {
            saveUserDataToFirestore(menuId, updateData);
        }


    }
    private void uploadMultipleImagesToCloudinary(MultipleImageUploadCallback callback) {
        ExecutorService executor = Executors.newFixedThreadPool(selectedImageUris.size());
        ArrayList<String> imageUrls = new ArrayList<>();

        for (Uri uri : selectedImageUris) {
            executor.execute(() -> {
                try (InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri)) {
                    Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                    synchronized (imageUrls) {
                        imageUrls.add((String) uploadResult.get("secure_url"));
                        if (imageUrls.size() == selectedImageUris.size()) {
                            this.runOnUiThread(() -> callback.onImagesUploaded(imageUrls));
                        }
                    }
                } catch (IOException e) {
                    this.runOnUiThread(() -> {
                        //  progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
    private void saveUserDataToFirestore(String menuId,Map<String, Object> updateData) {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .collection("Menu")
                .document(menuId)
                .update(updateData)
                .addOnCompleteListener(task -> {
                 //   progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivityUpdateMenuItemDetails.this, "Menu updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity
                    } else {
                        Toast.makeText(ActivityUpdateMenuItemDetails.this, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }
public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
        if (selectedImageUris == null) {
            selectedImageUris = new ArrayList<>();
        } else {
            selectedImageUris.clear();
        }

        if (data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            if (!selectedImageUris.isEmpty()) {
                itemImageView.setImageURI(selectedImageUris.get(0));
                areImagesSelected = true;
            } else {
                areImagesSelected = false;
            }
        }
    }
}
    private interface MultipleImageUploadCallback {
        void onImagesUploaded(ArrayList<String> imageUrls);
    }
}
