package com.example.user_restaurant.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.UserDoesNotLogIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuTab extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView itemImageView;
    private EditText itemNameEditText, itemPriceEditText, itemDescriptionEditText;
    private ArrayList<Uri> selectedImageUris;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String restaurantId, userId;
    private Cloudinary cloudinary;
    private boolean areImagesSelected = false;
    private ProgressDialog progressDialog;
    private AutoCompleteTextView autoCompleteTextView, autoCompleteTextView2;
    private String[] foodtype, mealtype;
    private ArrayAdapter<String> foodadapter, mealadapter;

    private EditText portionQuantity;
    private String portion;
    private AutoCompleteTextView unitAutoComplete;
    private String[] units = {"g", "kg", "ml", "L", "oz", "lb", "pcs", "serving", "cup", "tbsp"};


    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_tab, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        itemImageView = view.findViewById(R.id.imageView5);
        itemNameEditText = view.findViewById(R.id.ItemName);
        itemPriceEditText = view.findViewById(R.id.itemPrice);
        itemDescriptionEditText = view.findViewById(R.id.editTextText3);

        Button selectImageButton = view.findViewById(R.id.selectImgBtn);
        Button submitItemButton = view.findViewById(R.id.itemSubmit);

        Glide.with(this).load(R.drawable.nutrition).into(itemImageView);
        initCloudinary();

        portionQuantity = view.findViewById(R.id.portionQuantity);
        unitAutoComplete = view.findViewById(R.id.unitAutoComplete);

        // Set up unit dropdown
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,  // Use your existing dropdown item layout
                units
        );
        unitAutoComplete.setAdapter(unitAdapter);
        unitAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            unitAutoComplete.clearFocus();
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Submitting item...");
        progressDialog.setCancelable(false);

        selectedImageUris = new ArrayList<>();

        // Initialize food type dropdown
        foodtype = new String[]{"Select Food Type", "Vegetarian", "Non-Vegetarian", "Vegan", "Jain Food"};
        foodadapter = new ArrayAdapter<>(requireContext(), R.layout.food_drop_item, foodtype);
        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(foodadapter);
        autoCompleteTextView.setText(foodtype[0], false);

        // Initialize meal type dropdown
        mealtype = new String[]{"Select Meal Type", "Starters", "Main Course", "Side Dishes", "Desserts", "Snacks", "Specialty Items", "Beverages"};
        mealadapter = new ArrayAdapter<>(requireContext(), R.layout.food_drop_item, mealtype);
        autoCompleteTextView2 = view.findViewById(R.id.autoCompleteTextView2);
        autoCompleteTextView2.setAdapter(mealadapter);
        autoCompleteTextView2.setText(mealtype[0], false);

        // Select Multiple Images Button
        selectImageButton.setOnClickListener(v -> openImageChooser());

        // Submit Item Button
        submitItemButton.setOnClickListener(v -> {
            UserDoesNotLogIn.checkUserLogin(getActivity());
            if (!validationField()) {
                return;
            }

            progressDialog.show();
            submitItemButton.setVisibility(View.INVISIBLE);

            restaurantId = auth.getCurrentUser().getUid();
            String strItemName = itemNameEditText.getText().toString();
            String strItemPrice = itemPriceEditText.getText().toString();
            String strDescription = itemDescriptionEditText.getText().toString();

            // Modify food type and meal type values: store null if default option is selected.
            String selectedFoodType = autoCompleteTextView.getText().toString();
            String selectedMealType = autoCompleteTextView2.getText().toString();

            // Get portion data
            String quantity = portionQuantity.getText().toString().trim();
            String unit = unitAutoComplete.getText().toString().trim();
            portion = !quantity.isEmpty() && !unit.isEmpty()
                    ? quantity + " " + unit
                    : "Standard portion";

            Toast.makeText(getActivity(), "portion:"+portion, Toast.LENGTH_SHORT).show();

            if(selectedFoodType.equals("Select Food Type") || selectedFoodType.equals("Food Category")){
                selectedFoodType = null;
            }
            if(selectedMealType.equals("Select Meal Type")){
                selectedMealType = null;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("itemName", strItemName);
            userData.put("itemPrice", strItemPrice);
            userData.put("itemDescription", strDescription);
            userData.put("restaurantId", restaurantId);
            userData.put("foodType", selectedFoodType);
            userData.put("mealType", selectedMealType);
            userData.put("quantity", portion);
            userData.put("date", new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK).format(new Date()));

            if (areImagesSelected) {
                uploadMultipleImagesToCloudinary(imageUrls -> {
                    userData.put("foodImageUrls", imageUrls);
                    saveUserDataToFirestore(userData, submitItemButton);
                });
            } else {
                saveUserDataToFirestore(userData, submitItemButton);
            }
        });

        return view;
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

    private void uploadMultipleImagesToCloudinary(MultipleImageUploadCallback callback) {
        ExecutorService executor = Executors.newFixedThreadPool(selectedImageUris.size());
        ArrayList<String> imageUrls = new ArrayList<>();

        for (Uri uri : selectedImageUris) {
            executor.execute(() -> {
                try (InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri)) {
                    Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                    synchronized (imageUrls) {
                        imageUrls.add((String) uploadResult.get("secure_url"));
                        if (imageUrls.size() == selectedImageUris.size()) {
                            requireActivity().runOnUiThread(() -> callback.onImagesUploaded(imageUrls));
                        }
                    }
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void saveUserDataToFirestore(Map<String, Object> userData, Button submitItemButton) {
        String itemId = db.collection("RestaurantInformation").document(restaurantId).collection("Menu").document().getId();
        userData.put("itemId", itemId);

        db.collection("RestaurantInformation").document(restaurantId).collection("Menu").document(itemId)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    submitItemButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Item added successfully!", Toast.LENGTH_LONG).show();
                    clearControls();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    submitItemButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Failed to add item!", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUris.clear();

            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
                itemImageView.setImageURI(selectedImageUris.get(0)); // Display the first selected image
            } else if (data.getData() != null) {
                // Single image selected
                selectedImageUris.add(data.getData());
                itemImageView.setImageURI(selectedImageUris.get(0));
            }

            areImagesSelected = !selectedImageUris.isEmpty();
        }
    }

    private interface MultipleImageUploadCallback {
        void onImagesUploaded(ArrayList<String> imageUrls);
    }
    public boolean validationField() {
        boolean isValid = true;
        if (itemNameEditText.getText().toString().isEmpty()) {
            itemNameEditText.setError("Please Enter Item Name");
            isValid = false;
        }
        if (itemPriceEditText.getText().toString().isEmpty()) {
            itemPriceEditText.setError("Please Enter Item Price");
            isValid = false;
        }
        if (itemDescriptionEditText.getText().toString().isEmpty()) {
            itemDescriptionEditText.setError("Please Enter Item Description");
            isValid = false;
        }
        String quantity = portionQuantity.getText().toString().trim();
        if (quantity.isEmpty()) {
            portionQuantity.setError("Please enter quantity");
            isValid = false;
        } else {
            portionQuantity.setError(null);
        }

        // Validate unit selection
        String unit = unitAutoComplete.getText().toString().trim();
        if (unit.isEmpty()) {
            unitAutoComplete.setError("Please select a unit");
            isValid = false;
        } else {
            unitAutoComplete.setError(null);
        }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getActivity(), "Please Select Item Photos", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if ((autoCompleteTextView.getText().toString().equals("Select Food Type") || autoCompleteTextView.getText().toString().equals("Food Category")) && autoCompleteTextView2.getText().toString().equals("Select Meal Type")) {
            Toast.makeText(getActivity(), "Please select a Food or Meal Type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }
    public void clearControls(){
        itemNameEditText.setText("");
        itemPriceEditText.setText("");
        itemDescriptionEditText.setText("");
        Glide.with(this).load(R.drawable.nutrition).into(itemImageView);
        autoCompleteTextView2.setText(mealtype[0], false);
        autoCompleteTextView.setText(foodtype[0], false);
    }
}
