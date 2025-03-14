package com.example.user_restaurant.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterMenuList;
import com.example.user_restaurant.models.ModelMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActivityMenuList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterMenuList adapter;
    private List<ModelMenu> menuList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentRestaurantId = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a layout file named activity_menu_list.xml (see below)
        setContentView(R.layout.activity_menu_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Assume the restaurant ID is the current user's UID
        currentRestaurantId = auth.getCurrentUser().getUid();

        swipeRefreshLayout.setOnRefreshListener(this::loadMenus);

        loadMenus();
    }

    private void loadMenus() {
        db.collection("RestaurantInformation")
                .document(currentRestaurantId)
                .collection("Menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    menuList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ModelMenu menu = snapshot.toObject(ModelMenu.class);
                        if (menu != null) {
                            menuList.add(menu);
                        }
                    }
                    adapter = new AdapterMenuList(this, menuList);
                    recyclerView.setAdapter(adapter);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("ActivityMenuList", "Error loading menus: " + e.getMessage());
                    Toast.makeText(ActivityMenuList.this, "Error loading menus", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }
}
