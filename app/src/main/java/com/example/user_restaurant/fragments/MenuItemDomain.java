package com.example.user_restaurant.fragments;

public class MenuItemDomain {
    private String title;
    private String foodCat;
    private String mealCat;
    private String price;
    private String pic;

    public MenuItemDomain(String title, String foodCat, String mealCat, String price, String pic) {
        this.title = title;
        this.foodCat = foodCat;
        this.mealCat = mealCat;
        this.price = price;
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFoodCat() {
        return foodCat;
    }

    public void setFoodCat(String foodCat) {
        this.foodCat = foodCat;
    }

    public String getMealCat() {
        return mealCat;
    }

    public void setMealCat(String mealCat) {
        this.mealCat = mealCat;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
