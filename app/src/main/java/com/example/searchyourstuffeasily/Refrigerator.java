package com.example.searchyourstuffeasily;

import com.google.firebase.database.snapshot.Index;

import java.util.ArrayList;
import java.util.List;

public class Refrigerator extends Furniture {
    ArrayList<Food> Flist = new ArrayList<Food>();

    public Refrigerator(String name){
        super(name);
    }
    public ArrayList<Food> getFlist(){
        return Flist;
    }
    public void addFood(Food f, List<Food> foodList) {
        Flist.add(f);
        foodList.add(f);
    }

    public Food searchFoodById(String id){
        for (Food fTemp : Flist) {
            if (fTemp.getId().equals(id))
                return fTemp;
        }
        return null;
    }

    public Food searchFoodByIndex(int index){
        return Flist.get(index);
    }
}