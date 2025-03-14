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

public class complete_orders extends Fragment {
    private RecyclerView recyclerView;
    private AdapterPndOdr adapter;
    private List<ModelBooking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Variables to hold the current user's profile data
    private String currentUserName = "";
    private String currentUserPhone = "";
    private String currentUserEmail = "";
    private String currentUserProfileImageUrl = "";
    private String currentUserId = "";

    public complete_orders() {
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

        // First, fetch the current user's profile data from the "users" collection.
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("name");
                        currentUserPhone = documentSnapshot.getString("phone");
                        currentUserEmail = documentSnapshot.getString("email");
                        currentUserProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                    }
                    // After fetching user data, load bookings.
                    loadBookings();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    // Even if user data fetch fails, attempt to load bookings.
                    loadBookings();
                });

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
        return view;
    }

    private void loadBookings() {
        db.collection("RestaurantInformation").document(currentUserId)
                .collection("Bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ModelBooking booking = snapshot.toObject(ModelBooking.class);
                        // Only add the booking if its booking date is today
                        if (booking != null && isToday(booking.getBookingDate())) {
                            bookingList.add(booking);
                        }
                    }
                    // Create the adapter instance with the latest user profile data.
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
     * Checks whether the given date is today.
     */
    private boolean isToday(Date date) {
        Calendar bookingCal = Calendar.getInstance();
        bookingCal.setTime(date);

        Calendar today = Calendar.getInstance();
        return bookingCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                bookingCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                bookingCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }
}
