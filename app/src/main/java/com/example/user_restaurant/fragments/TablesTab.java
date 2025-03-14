package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;
import com.example.user_restaurant.UserDoesNotLogIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TablesTab extends Fragment {

    private ImageView tableImage;
    private EditText tableNoText;
    private Spinner spinner;
    private EditText waiterNameText;
    private TextView totalSeatsText; // New TextView for total seats
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId, restaurantId;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tables_tab, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tableImage = view.findViewById(R.id.imageView14);
        tableNoText = view.findViewById(R.id.editTextText);
        waiterNameText = view.findViewById(R.id.editTextText2);
        spinner = view.findViewById(R.id.spinner_seat);
        totalSeatsText = view.findViewById(R.id.totalSeatsText); // Ensure this TextView exists in your layout

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Submitting item...");
        progressDialog.setCancelable(false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button tableSubBtn = view.findViewById(R.id.table_sub_btn);

        // Populate the spinner with seat options (1 to 16)
        Integer[] seatOptions = new Integer[16];
        for (int i = 0; i < 16; i++) {
            seatOptions[i] = i + 1;
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, seatOptions);
        spinner.setAdapter(adapter);

        // Listener for seat selection to update table image accordingly
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedSeats = (int) parent.getItemAtPosition(position);
                updateTableImage(selectedSeats);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Load current total seats on fragment load
        updateTotalSeats();

        // Click on Add Table Button
        tableSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserDoesNotLogIn.checkUserLogin(getActivity());

                String tableNumber = tableNoText.getText().toString();
                String waiterName = waiterNameText.getText().toString();
                int selectedSeats = (int) spinner.getSelectedItem(); // Get selected seats as int

                if (tableNumber.isEmpty()) {
                    tableNoText.setError("Please Enter The table Number");
                    tableNoText.setFocusable(true);
                    return;
                }
                if (waiterName.isEmpty()) {
                    waiterNameText.setError("Please Enter The waiter name");
                    waiterNameText.setFocusable(true);
                    return;
                }

                if (!tableNumber.isEmpty() && !waiterName.isEmpty()) {
                    progressDialog.show(); // Show progress dialog
                    tableSubBtn.setVisibility(View.INVISIBLE); // Hide submit button while processing

                    SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
                    Date now = new Date();

                    userId = auth.getCurrentUser().getUid();
                    restaurantId = userId;  // Assuming restaurantId equals userId

                    // Generate a new document ID for the table under RestaurantInformation/restaurantId/Tables
                    String tableId = db.collection("RestaurantInformation").document(restaurantId)
                            .collection("Tables").document().getId();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("tableNumber", tableNumber);
                    userData.put("waiterName", waiterName);
                    userData.put("seats", String.valueOf(selectedSeats));
                    userData.put("userId", userId);
                    userData.put("tableId", tableId);
                    userData.put("date", format.format(now));
                    userData.put("restaurantId", restaurantId);

                    // Save table information into Firestore under RestaurantInformation/restaurantId/Tables
                    DocumentReference documentReference = db.collection("RestaurantInformation").document(restaurantId);
                    documentReference.collection("Tables").document(tableId).set(userData)
                            .addOnSuccessListener(unused -> {
                                progressDialog.dismiss(); // Hide progress dialog
                                tableSubBtn.setVisibility(View.VISIBLE); // Show submit button
                                Toast.makeText(getActivity(), "Table added successfully!", Toast.LENGTH_SHORT).show();

                                clearControls();
                                // Update the total seats after adding a new table
                                updateTotalSeats();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss(); // Hide progress dialog
                                tableSubBtn.setVisibility(View.VISIBLE); // Show submit button
                                Toast.makeText(getActivity(), "Failed to add Table: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                // Simple button animation on click
                tableSubBtn.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .alpha(0.8f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            tableSubBtn.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .alpha(1f)
                                    .setDuration(500)
                                    .start();
                        })
                        .start();
            }
        });

        return view;
    }

    // Method to update the table image based on selected seat count
    private void updateTableImage(int seats) {
        if (seats <= 4) {
            tableImage.setImageResource(R.drawable.four_table_1);
        } else if (seats == 5) {
            tableImage.setImageResource(R.drawable.five_table_1);
        } else if (seats == 6) {
            tableImage.setImageResource(R.drawable.six_table_1);
        } else if (seats <= 8) {
            tableImage.setImageResource(R.drawable.eight_table_1);
        } else if (seats <= 10) {
            tableImage.setImageResource(R.drawable.ten_table_1);
        } else if (seats <= 12) {
            tableImage.setImageResource(R.drawable.twel_table_1);
        } else if (seats <= 16) {
            tableImage.setImageResource(R.drawable.sixteen_table);
        }
    }

    // Method to clear input fields after table is added
    public void clearControls() {
        tableNoText.setText("");
        waiterNameText.setText("");
        spinner.setSelection(0);
    }

    // New method to update the total seats by fetching all table documents for the current restaurant
    private void updateTotalSeats() {
        userId = auth.getCurrentUser().getUid();
        restaurantId = userId;  // Assuming restaurantId equals userId
        Map<String,Object> map = new HashMap<>();
        // Query the "Tables" subcollection for the current restaurant
        db.collection("RestaurantInformation").document(restaurantId)
                .collection("Tables")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalSeats = 0;
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String seatsStr = document.getString("seats");
                        if (seatsStr != null) {
                            try {
                                totalSeats += Integer.parseInt(seatsStr);
                                map.put("totalNumberOfSeats",String.valueOf(totalSeats));
                                saveUserDataToFirestore(restaurantId, map);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    totalSeatsText.setText("Total Seats: " +totalSeats);
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to update total seats: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void saveUserDataToFirestore(String restaurantId, Map<String, Object> userData) {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .update(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

}
