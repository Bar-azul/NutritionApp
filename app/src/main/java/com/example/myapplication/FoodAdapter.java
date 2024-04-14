package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private List<FoodItem> foodItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodItem item, String itemName);
    }

    public FoodAdapter(List<FoodItem> foodItemList, OnItemClickListener listener) {
        this.foodItemList = foodItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);
        holder.bind(foodItem, listener);
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    public void updateList(List<FoodItem> newList) {
        foodItemList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView caloriesTextView;
        private TextView proteinTextView;
        private TextView carbohydratesTextView;
        private TextView fatTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            proteinTextView = itemView.findViewById(R.id.proteinTextView);
            carbohydratesTextView = itemView.findViewById(R.id.carbohydratesTextView);
            fatTextView = itemView.findViewById(R.id.fatTextView);
        }

        public void bind(final FoodItem foodItem, final OnItemClickListener listener) {
            nameTextView.setText(foodItem.getName());
            caloriesTextView.setText("Calories: " + foodItem.getCalories());
            proteinTextView.setText("Protein: " + foodItem.getProtein() + "g");
            carbohydratesTextView.setText("Carbohydrates: " + foodItem.getCarbohydrates() + "g");
            fatTextView.setText("Fat: " + foodItem.getFat() + "g");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(foodItem, foodItem.getName());
                }
            });
        }
    }
}
