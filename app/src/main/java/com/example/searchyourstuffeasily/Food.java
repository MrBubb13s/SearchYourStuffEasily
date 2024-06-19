package com.example.searchyourstuffeasily;

public class Food {
    private String id, name, locationInfo, expirationDate, imageUrl;
    private int count;

    public Food() {
        // Default constructor required for calls to DataSnapshot.getValue(Food.class)
    }
    public Food(String id, String name, String info, int count, String expirationDate) {
        this.id = id;
        this.name = name;
        this.locationInfo = info;
        this.count = count;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getLocationInfo() {
        return locationInfo;
    }
    public int getCount() {
        return count;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocationInfo(String info) {
        this.locationInfo = info;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}