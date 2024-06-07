package com.example.searchyourstuffeasily;

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

    public void removeFood(String id, List<Food> foodList) {
        for (int i = 0; i < Flist.size(); i++) {
            Food fTemp = Flist.get(i);
            if (fTemp.getId().equals(id)) {
                Flist.remove(i);
                foodList.remove(fTemp);
                break;
            }
        }
    }

    public Food searchFoodById(String id){
        for (Food fTemp : Flist) {
            if (fTemp.getId().equals(id))
                return fTemp;
        }
        return null;
    }
}