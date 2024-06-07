package com.example.searchyourstuffeasily;

public class Food {
    private String id, name, placeDetail, expirationDate;
    private int count;

    public Food() {
        // Default constructor required for calls to DataSnapshot.getValue(Food.class)
    }

    public Food(String id, String name, String placeDetail, int count, String expirationDate) {
        this.id = id;
        this.name = name;
        this.placeDetail = placeDetail;
        this.count = count;
        this.expirationDate = expirationDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceDetail() {
        return placeDetail;
    }

    public void setPlaceDetail(String placeDetail) {
        this.placeDetail = placeDetail;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }


    public void setId(String id) {
        this.id = id;
    }
}