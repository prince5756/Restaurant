package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.user_restaurant.R;
import com.example.user_restaurant.activities.ActivityBookingList;
import com.example.user_restaurant.activities.ActivityMenuList;
import com.example.user_restaurant.activities.ActivityShowRestaurantProfileDetails;
import com.example.user_restaurant.activities.Subscription;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This fragment displays restaurant info (profile pic, name) and 4 cards:
 * 1) Total Menus
 * 2) Total Orders
 * 3) Total Clients
 * 4) Upcoming Orders  <-- replaced static "Total Revenue" with upcoming bookings count
 *
 * The data is fetched from Firestore. Animations and swipe-to-refresh remain as in your original code.
 */
public class homefragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgProfile;
    private TextView txtName;
    private Uri profileImageUri;

    // Card references for the 4 metrics
    private CardView card1, card2, card3, card4;

    Boolean isPayment;
    Date expiryDate;
    TextView txtHead,txtTitle;
    ImageView img;
    MaterialCardView fmPayment;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homefragment, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtName);


        txtHead = view.findViewById(R.id.tvSubscriptionStatus);
        txtTitle = view.findViewById(R.id.btnSubscriptionAction); // Make sure this ID matches
        img = view.findViewById(R.id.img);
        fmPayment = view.findViewById(R.id.flpayment);

        // Initialize card references
        card1 = view.findViewById(R.id.card1); // Will display total menus
        card2 = view.findViewById(R.id.card2); // Will display total orders
        card3 = view.findViewById(R.id.card3); // Will display total clients
        card4 = view.findViewById(R.id.card4); // Will display upcoming orders (previously "Total Revenue")

        // Set up card click animations
        setupCardClick(card1, view);
        setupCardClick(card2, view);
        setupCardClick(card3, view);
        setupCardClick(card4, view);

        // Trigger initial card animations
        animateCardViews(card1, card2, card3, card4);




        // card1: show menu list
        card1.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ActivityMenuList.class));
        });

        // card2: show booking list
        card2.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ActivityBookingList.class));
        });

        // card4: show upcoming orders fragment (as originally done)
        card4.setOnClickListener(v -> {
            pending_oders fragment = new pending_oders();
            if(getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Load restaurant user data from Firestore
        loadUserData();
//        fetchRestaurantData();
        // On profile image click, show profile details
        imgProfile.setOnClickListener(view1 ->
                startActivity(new Intent(getActivity(), ActivityShowRestaurantProfileDetails.class))
        );

        // Swipe-to-refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        return view;
    }

    // Called by swipeRefreshLayout to refresh data
    private void refreshData() {
        loadUserData(); // Reload restaurant data (and metrics)
    }

    /**
     * loadUserData:
     *  - Fetches restaurant info (name, image URLs) from Firestore.
     *  - Then fetches the counts for menus, orders, clients, and upcoming orders.
     */
    private void loadUserData() {


        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        DocumentReference userRef = db.collection("RestaurantInformation").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Display restaurant name
                txtName.setText(documentSnapshot.getString("restaurantName"));

                Boolean isPayment = documentSnapshot.getBoolean("isPayment");

                if(isPayment==true){
                    img.setVisibility(View.GONE);
                    txtTitle.setText(documentSnapshot.getString("paymentValidity"));
                    txtHead.setText("Premium Plan Active until");
                }else{
                    img.setVisibility(View.VISIBLE);
                    txtTitle.setText("Subscribe Now");
                    txtHead.setText("Unlock Premium features");
                    fmPayment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getActivity(), Subscription.class));
                        }
                    });
                }
                // Display restaurant profile image (first in restaurantImageUrls, if present)
                List<String> imageUrls = (List<String>) documentSnapshot.get("restaurantImageUrls");
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrls.get(0))
                            .error(R.drawable.profile_pic)
                            .placeholder(R.drawable.profile_pic)
                            .into(imgProfile);
                } else {
                    Glide.with(this).load(R.drawable.profile_pic).into(imgProfile);
                }

                // Now fetch the metrics
                fetchTotalMenus(currentUserId);
                fetchOrdersAndClients(currentUserId);
            } else {
                // No doc found, set defaults
                txtName.setText("Restaurant");
                Glide.with(this).load(R.drawable.profile_pic).into(imgProfile);
                setCardText(card1, "Total Menus", "0");
                setCardText(card2, "Total Orders", "0");
                setCardText(card3, "Total Clients", "0");
                // Previously "Total Revenue"
                setCardText(card4, "Upcoming Orders", "0");
            }
            swipeRefreshLayout.setRefreshing(false);
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * Fetches total menus from: RestaurantInformation/{currentUserId}/Menu
     */
    private void fetchTotalMenus(String currentUserId) {
        db.collection("RestaurantInformation")
                .document(currentUserId)
                .collection("Menu")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalMenus = querySnapshot.size();
                    setCardText(card1, "Total Menus", String.valueOf(totalMenus));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to load menus: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Fetches total orders, total unique clients, and upcoming orders from: RestaurantInformation/{currentUserId}/Bookings
     */
    private void fetchOrdersAndClients(String currentUserId) {
        db.collection("RestaurantInformation")
                .document(currentUserId)
                .collection("Bookings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalOrders = querySnapshot.size();
                    setCardText(card2, "Total Orders", String.valueOf(totalOrders));

                    // Calculate total unique clients
                    Set<String> uniqueClients = new HashSet<>();
                    // Count upcoming orders
                    int upcomingCount = 0;

                    for (DocumentSnapshot doc : querySnapshot) {
                        String customerId = doc.getString("customerId");
                        if (customerId != null) {
                            uniqueClients.add(customerId);
                        }

                        // Check if booking is in the future
                        // We'll use "expiryTimestamp" to determine if it's upcoming
                        // Adjust if you prefer "bookingTimestamp" or another field
                        if (doc.contains("expiryTimestamp")) {
                            com.google.firebase.Timestamp ts = doc.getTimestamp("expiryTimestamp");
                            if (ts != null) {
                                Date bookingDate = ts.toDate();
                                if (isFuture(bookingDate)) {
                                    upcomingCount++;
                                }
                            }
                        }
                    }
                    setCardText(card3, "Total Clients", String.valueOf(uniqueClients.size()));
                    // Now set the upcoming orders count on card4
                    setCardText(card4, "Upcoming Orders", String.valueOf(upcomingCount));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to load orders/clients: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Helper to set text on each card:
     *  - The first child (TextView) is the label (e.g., "Total Menus")
     *  - The second child (TextView) is the value (e.g., "5")
     */
    private void setCardText(CardView cardView, String label, String value) {
        if (cardView.getChildCount() < 2) return;
        TextView textView = (TextView) cardView.getChildAt(0);
        TextView noTextview = (TextView) cardView.getChildAt(1);

        textView.setText(label);
        noTextview.setText(value);
    }

    /**
     * Determines if the given date/time is strictly after "today" (i.e., tomorrow or later).
     * If you want "strictly after now," adjust logic accordingly.
     */
    private boolean isFuture(Date date) {
        if (date == null) return false;

        // We define "future" as starting from tomorrow 00:00
        Calendar bookingCal = Calendar.getInstance();
        bookingCal.setTime(date);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        return bookingCal.getTime().compareTo(tomorrow.getTime()) >= 0;
    }

    /**
     * Sets up the click animation for each card. The text changes color/size upon click.
     */
    private void setupCardClick(CardView cardView, View rootView) {
        cardView.setOnClickListener(view -> {
            cardView.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .alpha(0.8f)
                    .setDuration(300)
                    .withEndAction(() -> cardView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(300)
                            .start())
                    .start();

            cardView.setCardBackgroundColor(requireContext().getResources().getColor(R.color.btn_bg_black));
            animateTextChange(cardView);
        });
    }

    /**
     * Animates the text in the card:
     *  - The first textView fades out, then changes color & size, then fades back in
     *  - The second textView is set to visible
     */
    private void animateTextChange(CardView cardView) {
        if (cardView.getChildCount() < 2) return;
        TextView textView = (TextView) cardView.getChildAt(0);
        TextView noTextview = (TextView) cardView.getChildAt(1);

        textView.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    noTextview.setVisibility(View.VISIBLE);
                    textView.setTextColor(requireContext().getResources().getColor(R.color.white));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    textView.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    /**
     * Animate the 4 cards in a staggered manner upon fragment creation
     */
    private void animateCardViews(CardView card1, CardView card2, CardView card3, CardView card4) {
        new Handler().postDelayed(() -> triggerCardAnimation(card1), 500);
        new Handler().postDelayed(() -> triggerCardAnimation(card2), 800);
        new Handler().postDelayed(() -> triggerCardAnimation(card3), 100);
        new Handler().postDelayed(() -> triggerCardAnimation(card4), 400);
    }

    /**
     * Single card animation: scales down slightly, fades, then scales back
     */
    private void triggerCardAnimation(CardView cardView) {
        cardView.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.8f)
                .setDuration(300)
                .withEndAction(() -> cardView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300)
                        .start())
                .start();

        cardView.setCardBackgroundColor(requireContext().getResources().getColor(R.color.btn_bg_black));
        animateTextChange(cardView);
    }
//    private void fetchRestaurantData() {
//        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
//        if (currentUserId == null) return;
//
//        db.collection("RestaurantInformation")
//                .document(currentUserId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    // Existing fields you're already using
//                    String restaurantName = documentSnapshot.getString("restaurantName");
//                    List<String> imageUrls = (List<String>) documentSnapshot.get("restaurantImageUrls");
//
//                    // Additional fields from your Firestore document
//                    String location = documentSnapshot.getString("location");
////                    String phone = documentSnapshot.getString("phone");
////                    String openingHours = documentSnapshot.getString("openingHours");
////                    Boolean idClosed = documentSnapshot.getBoolean("idClosed");
////                    Boolean idPayment = documentSnapshot.getBoolean("idPayment");
//                    txtHead.setText(location);
//
//                    // Use these values as needed (e.g., update UI/store in variables)
//                })
//                .addOnFailureListener(e -> {
//                    // Handle error
//                });
//    }
}
