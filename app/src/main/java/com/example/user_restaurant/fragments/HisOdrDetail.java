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

public class HisOdrDetail extends Fragment {
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

    public HisOdrDetail() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_history, container, false);

        // Ensure the layout contains a SwipeRefreshLayout (id: swipeRefreshLayout) and a RecyclerView (id: recyclerViewBookings)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Fetch current user's profile data from the "users" collection
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("name");
                        currentUserPhone = documentSnapshot.getString("phone");
                        currentUserEmail = documentSnapshot.getString("email");
                        currentUserProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                    }
                    // After fetching user data, load the history bookings
                    loadBookings();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    // Even if user data fails to load, attempt to load bookings
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
                        // Only add the booking if its date is in the past (before today)
                        if (booking != null && isPast(booking.getBookingDate())) {
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

    // Returns true if the booking date is before the start of today
    private boolean isPast(Date date) {
        Calendar bookingCal = Calendar.getInstance();
        bookingCal.setTime(date);

        // Set up today's start (00:00:00.000)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return bookingCal.getTime().before(today.getTime());
    }
}
