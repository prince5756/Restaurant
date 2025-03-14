package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterRestaurantInformation;
import com.example.user_restaurant.adapters.ImageAdapter;
import com.example.user_restaurant.models.ModelMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActivityRestaurantInformation extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterRestaurantInformation adapter;
    private ArrayList<ModelMenu> menuItemList;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_information);


        auth=FirebaseAuth.getInstance();
        userId=auth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerView);
        menuItemList = new ArrayList<>();
        adapter = new AdapterRestaurantInformation(this, menuItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Replace "YOUR_USER_ID" with your actual user ID
        String userId = "YOUR_USER_ID";  // This should be dynamically fetched (i.e., FirebaseAuth.getInstance().getCurrentUser().getUid())
        fetchMenuItems();
    }

    private void fetchMenuItems() {
        // Fetch data from the 'RestaurantInformation' collection for a specific user ID
        db.collection("RestaurantInformation").document(userId)
                .collection("Item")
                // Filter items by userId (if applicable)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Retrieve each document as a ModelMenu object
                                ModelMenu menuItem = document.toObject(ModelMenu.class);
                                menuItem.setItemId(document.getId()); // Set the document ID as itemId
                                menuItemList.add(menuItem);
                            }
                            // Notify the adapter that new data is added
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void displayImageSlider(ViewPager2 viewPager, List<String> imageUrls) {
        ImageAdapter sliderAdapter = new ImageAdapter(this, imageUrls);
        viewPager.setAdapter(sliderAdapter);
    }
}
