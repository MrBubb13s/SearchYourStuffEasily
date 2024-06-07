package com.example.searchyourstuffeasily;

import java.util.ArrayList;
import java.util.List;

public class Fridge {
    private String fridgeId, fridgeName;
    private List<Food> foodList;

    public Fridge() {
        // Default constructor required for calls to DataSnapshot.getValue(Fridge.class)
        this.foodList = new ArrayList<>();
    }

    public Fridge(String fridgeId, String fridgeName) {
        this.fridgeId = fridgeId;
        this.fridgeName = fridgeName;
        this.foodList = new ArrayList<>();
    }

    public String getFridgeId() {
        return fridgeId;
    }

    public String getFridgeName() {
        return fridgeName;
    }

    public List<Food> getFoodList() {
        return foodList;
    }
}