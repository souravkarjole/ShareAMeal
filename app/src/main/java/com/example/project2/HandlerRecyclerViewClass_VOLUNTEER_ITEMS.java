package com.example.project2;

import android.net.Uri;

public class HandlerRecyclerViewClass_VOLUNTEER_ITEMS {

    String foodCount;
    String restaurant;
    String username;
    Uri image;
    String order;


    public String getOrder() {
        return order;
    }

    public HandlerRecyclerViewClass_VOLUNTEER_ITEMS(String order, Uri image, String foodCount, String restaurant, String username) {
        this.foodCount = foodCount;
        this.restaurant = restaurant;
        this.username = username;
        this.image = image;
        this.order = order;
    }

    public Uri getImage() {
        return image;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public String getUsername() {
        return username;
    }

    public String getFoodCount() {
        return foodCount;
    }
}
