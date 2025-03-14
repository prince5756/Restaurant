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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a list of bookings in a RecyclerView
 * using a SwipeRefreshLayout for refreshing data.
 */
public class ActivityBookingList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterTotalNumberOfBookings adapter;
    private List<ModelBooking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        // Use the correct layout file that has R.id.recyclerViewBookings
        setContentView(R.layout.activity_bookig_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewBookings);
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
                    // After fetching user data, load the bookings from RestaurantInformation/{currentUserId}/Bookings
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
                    // Instantiate the adapter
                    adapter = new AdapterTotalNumberOfBookings(this, bookingList);
                    recyclerView.setAdapter(adapter);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}
