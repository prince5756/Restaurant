package com.example.user_restaurant.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CloseRestaurantActivity extends AppCompatActivity {

    private Button btnSelectDate, btnSubmit;
    private String selectedDate;
    private FirebaseFirestore db;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_restaurant);

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSubmit = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();
        // We assume the current user is the restaurant owner
        restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnSelectDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDatePickerDialog();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (selectedDate == null || selectedDate.isEmpty()) {
                    Toast.makeText(CloseRestaurantActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateClosedDate(selectedDate);
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CloseRestaurantActivity.this,
                new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        selectedDate = sdf.format(calendar.getTime());
                        btnSelectDate.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateClosedDate(String date){
        // Use FieldValue.arrayUnion to ensure closedDates is an array of strings.
        db.collection("RestaurantInformation")
                .document(restaurantId)
                .update("closedDates", FieldValue.arrayUnion(date))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CloseRestaurantActivity.this,
                            "Restaurant closed on " + date,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CloseRestaurantActivity.this,
                            "Failed to update: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
