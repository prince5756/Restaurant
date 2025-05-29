package com.example.user_restaurant.models;

import java.util.List;

public class ModelMenu {
    private String itemName;
    private String itemPrice;
    private String itemDescription;
    private String foodType;
    private String quantity;
    private String mealType;
    private String itemId;
    private String date;
    private List<String> foodImageUrls;
    private String restaurantId;
    public ModelMenu() {
    }

    public ModelMenu(String quantity, String itemName, String itemPrice, String itemDescription, String foodType, String mealType, String itemId, String date, List<String> foodImageUrls, String restaurantId) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemDescription = itemDescription;
        this.foodType = foodType;
        this.mealType = mealType;
        this.itemId = itemId;
        this.date = date;
        this.foodImageUrls = foodImageUrls;
        this.restaurantId = restaurantId;
        this.quantity = quantity;
    }

//    public String getFoodType() {
//        return foodType;
//    }
//
//    public void setFoodType(String foodType) {
//        this.foodType = foodType;
//    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getFoodImageUrls() {
        return foodImageUrls;
    }

    public void setFoodImageUrls(List<String> foodImageUrls) {
        this.foodImageUrls = foodImageUrls;
    }
}
