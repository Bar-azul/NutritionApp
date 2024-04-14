package com.example.myapplication;

public class FoodItem {
    private String name;
    private int calories;
    private double protein;
    private int carbohydrates;
    private double fat;

    public FoodItem(String name, int calories, double protein, int carbohydrates, double fat) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    public String getName() {
        return name;
    }

    public int getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public int getCarbohydrates() {
        return carbohydrates;
    }

    public double getFat() {
        return fat;
    }
}
