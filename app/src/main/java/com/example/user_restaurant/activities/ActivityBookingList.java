package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterTotalNumberOfBookings;
import com.example.user_restaurant.models.ModelBooking;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivityBookingList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterTotalNumberOfBookings adapter;
    private List<ModelBooking> bookingList;
    // Backup list to hold the complete set of bookings for filtering purposes
    private List<ModelBooking> allBookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PieChart pieChart;

    // Variables to store current user's profile data
    private String currentUserName = "";
    private String currentUserPhone = "";
    private String currentUserEmail = "";
    private String currentUserProfileImageUrl = "";
    private String currentUserId = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the correct layout file that includes both the PieChart and RecyclerView
        setContentView(R.layout.activity_bookig_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewBookings);
        pieChart = findViewById(R.id.pieChartBookings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Fetch user data first
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("name");
                        currentUserPhone = documentSnapshot.getString("phone");
                        currentUserEmail = documentSnapshot.getString("email");
                        currentUserProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                    }
                    // After fetching user data, load the bookings
                    loadBookings();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    // Even if user data fails to load, still attempt to load bookings
                    loadBookings();
                });

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
    }

    private void loadBookings() {
        db.collection("RestaurantInformation").document(currentUserId)
                .collection("Bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ModelBooking booking = snapshot.toObject(ModelBooking.class);
                        if (booking != null) {
                            bookingList.add(booking);
                        }
                    }
                    // Backup the complete list for filtering
                    allBookingList = new ArrayList<>(bookingList);

                    // Instantiate the adapter to display the bookings list
                    adapter = new AdapterTotalNumberOfBookings(this, bookingList);
                    recyclerView.setAdapter(adapter);

                    // Generate and display the monthly pie chart
                    generatePieChartData();

                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void generatePieChartData() {
        // Map to hold count of bookings for each month
        Map<String, Integer> monthCountMap = new HashMap<>();

        // Use a SimpleDateFormat to extract month (e.g., "Jan", "Feb", etc.)
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        for (ModelBooking booking : bookingList) {
            if (booking.getBookingDate() != null) {
                String month = monthFormat.format(booking.getBookingDate());
                int count = monthCountMap.getOrDefault(month, 0);
                monthCountMap.put(month, count + 1);
            }
        }

        // Prepare pie chart entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthCountMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Monthly Bookings");
        // Optionally, set colors for the dataset.
        // You can use the built-in color templates (here we use MATERIAL_COLORS)
        dataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(getResources().getColor(android.R.color.white));

        // Set data to the chart and refresh it
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Bookings per Month");
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh the chart

        // Add listener for filtering data when a pie slice is selected
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    String selectedMonth = ((PieEntry) e).getLabel();
                    List<ModelBooking> filteredList = new ArrayList<>();
                    // Filter bookings based on the selected month
                    for (ModelBooking booking : allBookingList) {
                        if (booking.getBookingDate() != null) {
                            String bookingMonth = monthFormat.format(booking.getBookingDate());
                            if (bookingMonth.equals(selectedMonth)) {
                                filteredList.add(booking);
                            }
                        }
                    }
                    // Update the recycler view with the filtered list
                    bookingList.clear();
                    bookingList.addAll(filteredList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected() {
                // Reset to show all bookings when no slice is selected
                bookingList.clear();
                bookingList.addAll(allBookingList);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
