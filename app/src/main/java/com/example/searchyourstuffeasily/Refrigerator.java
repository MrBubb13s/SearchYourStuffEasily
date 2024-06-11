package com.example.searchyourstuffeasily;

import java.util.ArrayList;

public class Refrigerator extends Furniture {
    private final ArrayList<Food> Flist = new ArrayList<Food>();

    public Refrigerator(String name){
        super(name);
    }


    public ArrayList<Food> getFlist(){
        return Flist;
    }
    public int getFlistSize(){
        return Flist.size();
    }
    public int getIndexByFood(Food food){
        return Flist.indexOf(food);
    }
    public Food getFoodByIndex(int index){
        return Flist.get(index);
    }

    public void addFood(Food food) {
        Flist.add(food);
    }

    public void updateFood(int index, Food food){
        Flist.set(index, food);
    }

    public void deleteFood(int index){
        Flist.remove(index);
    }

    public Food searchFoodById(String id){
        for (Food fTemp : Flist) {
            if (fTemp.getId().equals(id))
                return fTemp;
        }
        return null;
    }
}