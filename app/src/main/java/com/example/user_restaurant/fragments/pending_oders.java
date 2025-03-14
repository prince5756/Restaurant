package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterPndOdr;
import com.example.user_restaurant.models.ModelBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class pending_oders extends Fragment {

    private RecyclerView recyclerView;
    private AdapterPndOdr adapter;
    private List<ModelBooking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Variables to hold the current user's profile data (from the "users" collection)
    private String currentUserName = "";
    private String currentUserPhone = "";
    private String currentUserEmail = "";
    private String currentUserProfileImageUrl = "";
    private String currentUserId = "";

    public pending_oders() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_oders, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // First, fetch the current user's profile data from the "users" collection
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
        return view;
    }

    private void loadBookings() {
        // Fetch bookings stored under "RestaurantInformation/{currentUserId}/Bookings"
        db.collection("RestaurantInformation").document(currentUserId)
                .collection("Bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ModelBooking booking = snapshot.toObject(ModelBooking.class);
                        // Only add the booking if its booking date is in the future (tomorrow or later)
                        if (booking != null && isFuture(booking.getBookingDate())) {
                            bookingList.add(booking);
                        }
                    }
                    // Instantiate the adapter with the latest user profile data
                    adapter = new AdapterPndOdr(getContext(), bookingList);
                    recyclerView.setAdapter(adapter);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    /**
     * Returns true if the given booking date is on a future day (tomorrow or later).
     */
    private boolean isFuture(Date date) {
        if (date == null) return false;

        Calendar bookingCal = Calendar.getInstance();
        bookingCal.setTime(date);

        // Set up a calendar for the start of tomorrow (00:00:00.000)
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        return bookingCal.getTime().compareTo(tomorrow.getTime()) >= 0;
    }
}
