package com.example.user_restaurant.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.user_restaurant.adapters.MealCatAdapter;
import com.example.user_restaurant.R;
import com.example.user_restaurant.adapters.AdapterMenu;
import com.example.user_restaurant.adapters.foodCatAdapter;
import com.example.user_restaurant.models.FoodCategoryDomain;
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
    // Food category list adapter
    private RecyclerView.Adapter adapter, adapter3;
    private RecyclerView recyclerViewFood, recyclerViewMeal, recyclerViewMenu;
    String userId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AdapterMenu menuAdapter;
    // List for the filtered menu items (displayed in recyclerViewMenu)
    private ArrayList<ModelMenu> menuList;
    // Full list of menu items (used as the source for filtering)
    private ArrayList<ModelMenu> allMenuList;
    // Adapter for displaying unique meal types and handling selection for filtering
    private MealCatAdapter mealTypeAdapter;
    // TextView to show "Item not found" message
    private TextView textViewNotFound;
    // TextViews for "Food Categories" and "Meal Category" labels
    private TextView textViewFoodCategory, textViewMealCategory;
    // Food category list for filtering
    private ArrayList<FoodCategoryDomain> foodCategoryList;
    // Variables to store selected filters
    private String selectedFoodCategory = null;
    private String selectedMealCategory = null;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favfragment, container, false);

        // Initialize UI components
        textViewTitle = rootView.findViewById(R.id.textView20);
        editTextSearch = rootView.findViewById(R.id.search_box);
        imageViewSearch = rootView.findViewById(R.id.search_icon);
        constraintLayout = rootView.findViewById(R.id.constraintLayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        textViewNotFound = rootView.findViewById(R.id.textViewNotFound);
        textViewNotFound.setVisibility(View.GONE);

        // Get the labels for food and meal categories
        textViewFoodCategory = rootView.findViewById(R.id.textView22);
        textViewMealCategory = rootView.findViewById(R.id.textView23);

        recyclerViewFood = rootView.findViewById(R.id.recyclerView);
        recyclerViewMeal = rootView.findViewById(R.id.recycleView2);
        recyclerViewMenu = rootView.findViewById(R.id.recycleView3);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(getActivity()));

        allMenuList = new ArrayList<>();
        menuList = new ArrayList<>();
        menuAdapter = new AdapterMenu(getActivity(), menuList);
        recyclerViewMenu.setAdapter(menuAdapter);
        // Initially hide EditText
        editTextSearch.setVisibility(View.GONE);

        // Add search functionality: update filters when user types
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used.
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used.
            }
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                if (query.isEmpty()) {
                    // Show food and meal category views when search query is empty
                    recyclerViewFood.setVisibility(View.VISIBLE);
                    recyclerViewMeal.setVisibility(View.VISIBLE);
                    textViewFoodCategory.setVisibility(View.VISIBLE);
                    textViewMealCategory.setVisibility(View.VISIBLE);
                } else {
                    // Hide food and meal category views when searching
                    recyclerViewFood.setVisibility(View.GONE);
                    recyclerViewMeal.setVisibility(View.GONE);
                    textViewFoodCategory.setVisibility(View.GONE);
                    textViewMealCategory.setVisibility(View.GONE);
                }
                filterMenu();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        // Set onClickListener for the search icon
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateSearchTransition();
            }
        });

        // Set up food category RecyclerView with updated adapter
        recyclerViewFood();
        // Set up meal category RecyclerView (remains unchanged)
        recyclerViewMeal();
        // Load Menu Items from Firestore
        fetchMenuItems();
        return rootView;
    }

    private void refreshData() {
        fetchMenuItems(); // Reload restaurant data
    }

    private void recyclerViewFood() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFood.setLayoutManager(linearLayoutManager);

        foodCategoryList = new ArrayList<>();
        foodCategoryList.add(new FoodCategoryDomain("Vegetarian", "vegetarian"));
        foodCategoryList.add(new FoodCategoryDomain("Non-Veg", "non-veg"));
        foodCategoryList.add(new FoodCategoryDomain("Vegan", "vegan"));
        foodCategoryList.add(new FoodCategoryDomain("Jain", "jain"));

        foodCatAdapter fcAdapter = new foodCatAdapter(foodCategoryList);
        fcAdapter.setOnFoodCategoryClickListener(new foodCatAdapter.OnFoodCategoryClickListener() {
            @Override
            public void onFoodCategoryClick(FoodCategoryDomain category, boolean isSelected) {
                if (isSelected) {
                    selectedFoodCategory = category.getTitle();
                } else {
                    selectedFoodCategory = null;
                }
                filterMenu();
            }
        });
        adapter = fcAdapter;
        recyclerViewFood.setAdapter(adapter);
    }

    private void recyclerViewMeal() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMeal.setLayoutManager(linearLayoutManager);

        mealTypeAdapter = new MealCatAdapter(allMenuList);
        mealTypeAdapter.setOnMealTypeClickListener(new MealCatAdapter.OnMealTypeClickListener() {
            @Override
            public void onMealTypeClick(String mealType, boolean isSelected) {
                if (isSelected) {
                    selectedMealCategory = mealType;
                } else {
                    selectedMealCategory = null;
                }
                filterMenu();
            }
        });
        recyclerViewMeal.setAdapter(mealTypeAdapter);
    }

    private void animateSearchTransition() {
        int toXDelta = (int) (getResources().getDisplayMetrics().density * -50);
        TranslateAnimation moveAnimation = new TranslateAnimation(0, toXDelta, 0, 0);
        moveAnimation.setDuration(300);
        moveAnimation.setFillAfter(true);

        moveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                textViewTitle.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                editTextSearch.setVisibility(View.VISIBLE);
                imageViewSearch.clearAnimation();

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.clear(R.id.search_icon, ConstraintSet.END);
                constraintSet.connect(R.id.search_icon, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 30);
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
                        allMenuList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelMenu item = document.toObject(ModelMenu.class);
                            allMenuList.add(item);
                        }
                        filterMenu();
                        if (mealTypeAdapter != null) {
                            mealTypeAdapter.updateData(allMenuList);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to load menu!", Toast.LENGTH_SHORT).show();
                    }
                });
        swipeRefreshLayout.setRefreshing(false);
    }

    // Filter menu items based on search query, selected food category, and selected meal category
    private void filterMenu() {
        String query = editTextSearch.getText().toString().toLowerCase();
        menuList.clear();
        for (ModelMenu item : allMenuList) {
            boolean matchesQuery = true;
            boolean matchesFood = true;
            boolean matchesMeal = true;

            if (!query.isEmpty()) {
                matchesQuery = item.getItemName() != null && item.getItemName().toLowerCase().contains(query);
            }
            if (selectedFoodCategory != null) {
                matchesFood = item.getFoodType() != null && item.getFoodType().equalsIgnoreCase(selectedFoodCategory);
            }
            if (selectedMealCategory != null) {
                matchesMeal = item.getMealType() != null && item.getMealType().equals(selectedMealCategory);
            }
            if (matchesQuery && matchesFood && matchesMeal) {
                menuList.add(item);
            }
        }
        if (menuList.isEmpty()) {
            recyclerViewMenu.setVisibility(View.GONE);
            textViewNotFound.setVisibility(View.VISIBLE);
        } else {
            recyclerViewMenu.setVisibility(View.VISIBLE);
            textViewNotFound.setVisibility(View.GONE);
        }
        menuAdapter.notifyDataSetChanged();
    }
}
