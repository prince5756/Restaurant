package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_restaurant.MealCatAdapter;
//import com.example.user_restaurant.MenuItemAdapter;
import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterMenu;
import com.example.user_restaurant.foodCatAdapter;
import com.example.user_restaurant.models.ModelMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class favfragment extends Fragment {

    private TextView textViewTitle;
    private EditText editTextSearch;
    private ImageView imageViewSearch;
    private ConstraintLayout constraintLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    //food category list
    private RecyclerView.Adapter adapter, adapter2, adapter3;
    private RecyclerView recyclerViewFood, recyclerViewMeal, recyclerViewMenu;
    String userId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AdapterMenu menuAdapter;
    private ArrayList<ModelMenu> menuList;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favfragment, container, false);

        // Initialize UI components
        textViewTitle = rootView.findViewById(R.id.textView20);
        editTextSearch = rootView.findViewById(R.id.search_box);
        imageViewSearch = rootView.findViewById(R.id.search_icon);
        constraintLayout = rootView.findViewById(R.id.constraintLayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout); // Find SwipeRefreshLayout

        recyclerViewFood = rootView.findViewById(R.id.recyclerView);
        recyclerViewMeal = rootView.findViewById(R.id.recycleView2);
        recyclerViewMenu = rootView.findViewById(R.id.recycleView3);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(getActivity()));

        menuList = new ArrayList<>();
        menuAdapter = new AdapterMenu(getActivity(), menuList);
        recyclerViewMenu.setAdapter(menuAdapter);
        // Initially hide EditText
        editTextSearch.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        // Set onClickListener for the search icon
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateSearchTransition();
            }
        });
        //food category
        recyclerViewFood();

        //Meal category
        recyclerViewMeal();

        //Menu's Items
        //recyclerViewMenu();
        fetchMenuItems();
        return rootView;


    }
    private void refreshData() {
        fetchMenuItems(); // Reload restaurant data
    }


    private void recyclerViewFood() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMeal.setLayoutManager(linearLayoutManager);

        ArrayList<FoodCategoryDomain> foodCategoryList = new ArrayList<>();
        foodCategoryList.add(new FoodCategoryDomain("Vegetarian", "vegetarian"));
        foodCategoryList.add(new FoodCategoryDomain("Non-Veg", "non-veg"));
        foodCategoryList.add(new FoodCategoryDomain("Vegan", "vegan"));
        foodCategoryList.add(new FoodCategoryDomain("Jain", "jain"));

        adapter = new foodCatAdapter(foodCategoryList);
        recyclerViewFood.setAdapter(adapter);
    }

    private void recyclerViewMeal() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFood.setLayoutManager(linearLayoutManager);

        ArrayList<MealCategoryDomain> mealCategoryList = new ArrayList<>();
        mealCategoryList.add(new MealCategoryDomain("Starters"));
        mealCategoryList.add(new MealCategoryDomain("Main Course"));
        mealCategoryList.add(new MealCategoryDomain("Side Dishes"));
        mealCategoryList.add(new MealCategoryDomain("Desserts"));
        mealCategoryList.add(new MealCategoryDomain("Snacks"));
        mealCategoryList.add(new MealCategoryDomain("Specialty Items"));
        mealCategoryList.add(new MealCategoryDomain("Beverages"));

        adapter2 = new MealCatAdapter(mealCategoryList);
        recyclerViewMeal.setAdapter(adapter2);
    }

//    private void recyclerViewMenu() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
//        recyclerViewMenu.setLayoutManager(linearLayoutManager);
//
//        ArrayList<MenuItemDomain> menuItemList = new ArrayList<>();
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//        menuItemList.add(new MenuItemDomain("Kadhai Paneer Tukda", " • Vegetarian", " • Main Course", "₹ 450", "food"));
//
//        adapter3 = new MenuItemAdapter(menuItemList);
//        recyclerViewMenu.setAdapter(adapter3);
//
//    }



    private void animateSearchTransition() {
        // Animate the search icon to move to the left with 10dp margin
        int toXDelta = (int) (getResources().getDisplayMetrics().density * -50); // Adjust for dp
        TranslateAnimation moveAnimation = new TranslateAnimation(0, toXDelta, 0, 0);
        moveAnimation.setDuration(300); // Slow motion effect
        moveAnimation.setFillAfter(true);

        moveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Hide the TextView title during animation
                textViewTitle.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Make EditText visible after animation completes
                editTextSearch.setVisibility(View.VISIBLE);

                // Clear the animation to ensure it doesn't affect layout
                imageViewSearch.clearAnimation();

                // Dynamically update constraints
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);

                // Remove the end constraint and add a start constraint
                constraintSet.clear(R.id.search_icon, ConstraintSet.END);
                constraintSet.connect(R.id.search_icon, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30);

                // Apply the updated constraints
                constraintSet.applyTo(constraintLayout);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not needed
            }
        });

        imageViewSearch.startAnimation(moveAnimation);
    }
    private void fetchMenuItems() {

        db.collection("RestaurantInformation").document(userId)
                .collection("Menu")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        menuList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelMenu item = document.toObject(ModelMenu.class);
                            menuList.add(item);
                        }
                        menuAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Failed to load menu!", Toast.LENGTH_SHORT).show();
                    }
                });
        swipeRefreshLayout.setRefreshing(false);
    }
}