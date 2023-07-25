package com.example.project2;

import android.net.Uri;

import java.util.List;

public class HandlerRecyclerViewClass_DONAR {

    String foodCount;
    String restaurant;
    String username;
    Uri image;
    String order;

    public HandlerRecyclerViewClass_DONAR(String order,Uri image, String foodCount, String restaurant, String username) {
        this.order = order;
        this.foodCount = foodCount;
        this.restaurant = restaurant;
        this.username = username;
        this.image = image;
    }

    public String getOrder() {
        return order;
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
