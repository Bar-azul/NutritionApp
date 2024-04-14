package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class NutritionActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private AutoCompleteTextView searchAutoCompleteTextView;
    private EditText amountEditText;
    private Button addButton;
    private Button saveButton;
    private Button logoutButton;
    private Button newButton;
    private LinearLayout foodItemsLayout;
    private TextView totalNutritionTextView;

    private List<FoodItem> foodItemList = new ArrayList<>();
    private List<String> foodNames = new ArrayList<>();
    private Map<String, Integer> addedFoodItems = new HashMap<>();
    private DatabaseReference userFoodItemsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        usernameTextView = findViewById(R.id.usernameTextView);
        searchAutoCompleteTextView = findViewById(R.id.searchAutoCompleteTextView);
        amountEditText = findViewById(R.id.amountEditText);
        addButton = findViewById(R.id.addButton);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        newButton = findViewById(R.id.clearButton);
        foodItemsLayout = findViewById(R.id.foodItemsLayout);
        totalNutritionTextView = findViewById(R.id.totalNutritionTextView);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not signed in
            startActivity(new Intent(NutritionActivity.this, MainActivity.class));
            finish();
            return;
        }
        String username = currentUser.getEmail();
        usernameTextView.setText("Welcome, " + username + "!");
        userFoodItemsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("foodItems");

        readFoodItemsFromJson();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foodNames);
        searchAutoCompleteTextView.setAdapter(adapter);

        searchAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String amountString = amountEditText.getText().toString();
                        if (!TextUtils.isEmpty(amountString)) {
                            int amount = Integer.parseInt(amountString);
                            addFoodItem(selectedItem, amount);
                            updateFoodItemsLayout();
                            updateTotalNutrition();
                        } else {
                            Toast.makeText(NutritionActivity.this, "Please enter amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFoodItemsToDatabase();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(NutritionActivity.this, MainActivity.class));
                finish();
                Toast.makeText(NutritionActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            }
        });

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedFoodItems.clear();
                updateFoodItemsLayout();
                updateTotalNutrition();
            }
        });

        // Load food items from database if available
        userFoodItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String foodName = snapshot.getKey();
                        long amount = (long) snapshot.getValue();
                        addedFoodItems.put(foodName, (int) amount);
                    }
                    updateFoodItemsLayout();
                    updateTotalNutrition();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NutritionActivity.this, "Failed to load food items: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readFoodItemsFromJson() {
        try {
            InputStream inputStream = getAssets().open("food_items.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                double calories = jsonObject.getDouble("calories");
                double protein = jsonObject.getDouble("protein");
                double carbohydrates = jsonObject.getDouble("carbohydrates");
                double fat = jsonObject.getDouble("fat");
                foodItemList.add(new FoodItem(name, (int) calories, protein, (int) carbohydrates, fat));
                foodNames.add(name);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read food items from JSON file", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFoodItem(String foodName, int amount) {
        if (addedFoodItems.containsKey(foodName)) {
            addedFoodItems.put(foodName, addedFoodItems.get(foodName) + amount);
        } else {
            addedFoodItems.put(foodName, amount);
        }
    }

    private void updateFoodItemsLayout() {
        foodItemsLayout.removeAllViews();
        for (Map.Entry<String, Integer> entry : addedFoodItems.entrySet()) {
            String foodName = entry.getKey();
            int amount = entry.getValue();
            for (FoodItem foodItem : foodItemList) {
                if (foodItem.getName().equals(foodName)) {
                    TextView textView = new TextView(this);
                    textView.setText(amount + "g of " + foodItem.getName() + ": " +
                            "Calories: " + (foodItem.getCalories() * amount / 100.0) + " | " +
                            "Protein: " + (foodItem.getProtein() * amount / 100.0) + "g | " +
                            "Carbohydrates: " + (foodItem.getCarbohydrates() * amount / 100.0) + "g | " +
                            "Fat: " + (foodItem.getFat() * amount / 100.0) + "g");
                    foodItemsLayout.addView(textView);
                    break;
                }
            }
        }
    }


    private void updateTotalNutrition() {
        double totalCalories = 0;
        double totalFat = 0;
        double totalCarbs = 0;
        double totalProtein = 0;

        for (Map.Entry<String, Integer> entry : addedFoodItems.entrySet()) {
            String foodName = entry.getKey();
            int amount = entry.getValue();
            for (FoodItem foodItem : foodItemList) {
                if (foodItem.getName().equals(foodName)) {
                    totalCalories += (foodItem.getCalories() * amount) / 100.0;
                    totalFat += (foodItem.getFat() * amount) / 100.0;
                    totalCarbs += (foodItem.getCarbohydrates() * amount) / 100.0;
                    totalProtein += (foodItem.getProtein() * amount) / 100.0;
                    break;
                }
            }
        }

        String totalNutrition = "Total Nutrition:\n" +
                "Calories: " + totalCalories + " kcal\n" +
                "Fat: " + Math.round(totalFat*10)/10.0 + " g\n" +
                "Carbs: " + totalCarbs + " g\n" +
                "Protein: " + totalProtein + " g";

        totalNutritionTextView.setText(totalNutrition);
    }

    private void saveFoodItemsToDatabase() {
        userFoodItemsRef.setValue(addedFoodItems)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data successfully saved
                        Toast.makeText(NutritionActivity.this, "Food items saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(NutritionActivity.this, "Failed to save food items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

