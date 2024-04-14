package com.example.myapplication;

import java.util.Map;

public class User {
    private String email;
    private String password;
    private Map<FoodItem, Integer> addedFoodItems; // Added this field

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<FoodItem, Integer> getAddedFoodItems() {
        return addedFoodItems;
    }

    public void setAddedFoodItems(Map<FoodItem, Integer> addedFoodItems) {
        this.addedFoodItems = addedFoodItems;
    }
}
