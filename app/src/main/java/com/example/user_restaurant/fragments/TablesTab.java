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

import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.UserDoesNotLogIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class TablesTab extends Fragment {

    private ImageView tableImage;
    private EditText tableNoText;
    private Spinner spinner_waiter;  // Spinner for waiter names (from Firestore)
    private Spinner spinner_seat;    // Spinner for seat selection
    private TextView totalSeatsText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId, restaurantId;
    private ProgressDialog progressDialog;

    private ArrayList<String> waiterNameList;  // Will hold waiter names from Firestore
    private ArrayAdapter<String> waiterAdapter; // Adapter for waiter spinner

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tables_tab, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tableImage      = view.findViewById(R.id.imageView14);
        tableNoText     = view.findViewById(R.id.editTextText);
        spinner_waiter  = view.findViewById(R.id.spinner_waiter);
        spinner_seat    = view.findViewById(R.id.spinner_seat);
        totalSeatsText  = view.findViewById(R.id.totalSeatsText);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Submitting item...");
        progressDialog.setCancelable(false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button tableSubBtn = view.findViewById(R.id.table_sub_btn);

        // STEP 1: Populate spinner_seat with seat options (1 to 16), same as before
        Integer[] seatOptions = new Integer[16];
        for (int i = 0; i < 16; i++) {
            seatOptions[i] = i + 1;
        }
        ArrayAdapter<Integer> seatAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                seatOptions
        );
        spinner_seat.setAdapter(seatAdapter);

        spinner_seat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View innerView, int position, long id) {
                int selectedSeats = (int) parent.getItemAtPosition(position);
                updateTableImage(selectedSeats);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // STEP 2: Initialize the waiterNameList and default adapter
        waiterNameList = new ArrayList<>();
        // Optionally add a default "Select Waiter" item
        waiterNameList.add("Select Waiter");
        waiterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                waiterNameList
        );
        spinner_waiter.setAdapter(waiterAdapter);

        // STEP 3: Fetch waiter names from Firestore
        loadWaitersFromFirestore();

        // Load current total seats on fragment load
        updateTotalSeats();

        // Click on Add Table Button
        tableSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserDoesNotLogIn.checkUserLogin(getActivity());

                String tableNumber = tableNoText.getText().toString();
                String waiterName = spinner_waiter.getSelectedItem().toString();
                int selectedSeats = (int) spinner_seat.getSelectedItem();

                if (tableNumber.isEmpty()) {
                    tableNoText.setError("Please Enter The table Number");
                    tableNoText.requestFocus();
                    return;
                }
                // Validate waiter selection
                if (waiterName.equals("Select Waiter")) {
                    Toast.makeText(getActivity(), "Please select the waiter name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!tableNumber.isEmpty() && !waiterName.isEmpty()) {
                    progressDialog.show();
                    tableSubBtn.setVisibility(View.INVISIBLE);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
                    Date now = new Date();

                    userId       = auth.getCurrentUser().getUid();
                    restaurantId = userId;

                    // Generate a new document ID for the table under RestaurantInformation/restaurantId/Tables
                    String tableId = db.collection("RestaurantInformation")
                            .document(restaurantId)
                            .collection("Tables")
                            .document()
                            .getId();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("tableNumber", tableNumber);
                    userData.put("waiterName", waiterName);
                    userData.put("seats", String.valueOf(selectedSeats));
                    userData.put("userId", userId);
                    userData.put("tableId", tableId);
                    userData.put("date", format.format(now));
                    userData.put("restaurantId", restaurantId);

                    DocumentReference docRef = db.collection("RestaurantInformation")
                            .document(restaurantId);

                    docRef.collection("Tables").document(tableId).set(userData)
                            .addOnSuccessListener(unused -> {
                                progressDialog.dismiss();
                                tableSubBtn.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), "Table added successfully!", Toast.LENGTH_SHORT).show();
                                clearControls();
                                updateTotalSeats();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                tableSubBtn.setVisibility(View.VISIBLE);
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

    // Fetch waiter names from Firestore: RestaurantInformation -> userId -> Employee -> empId
    private void loadWaitersFromFirestore() {
        userId = auth.getCurrentUser().getUid();
        CollectionReference employeesRef = db.collection("RestaurantInformation")
                .document(userId)
                .collection("Employee");

        employeesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear existing items except the default "Select Waiter"
                    // waiterNameList[0] is "Select Waiter", keep it
                    if (waiterNameList.size() > 1) {
                        waiterNameList.subList(1, waiterNameList.size()).clear();
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            waiterNameList.add(name);
                        }
                    }
                    // Notify the spinner's adapter that the data has changed
                    waiterAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load waiters: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Update the table image based on selected seat count
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

    // Clear input fields after table is added
    public void clearControls() {
        tableNoText.setText("");
        // Reset spinner_waiter to "Select Waiter"
        spinner_waiter.setSelection(0);
        // Reset seat spinner to first seat option
        spinner_seat.setSelection(0);
    }

    // Update the total seats by fetching all table documents for the current restaurant
    private void updateTotalSeats() {
        userId = auth.getCurrentUser().getUid();
        restaurantId = userId;
        Map<String, Object> map = new HashMap<>();

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
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Update Firestore with the new total
                    map.put("totalNumberOfSeats", String.valueOf(totalSeats));
                    saveUserDataToFirestore(restaurantId, map);

                    totalSeatsText.setText("Total Seats: " + totalSeats);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to update total seats: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserDataToFirestore(String restaurantId, Map<String, Object> userData) {
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .update(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // No UI changes needed on success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No UI changes needed on failure, but you can log or toast
                    }
                });
    }
}
