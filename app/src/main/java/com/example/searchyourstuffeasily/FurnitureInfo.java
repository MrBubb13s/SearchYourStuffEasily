// FurnitureInfo.java
package com.example.searchyourstuffeasily;

public class FurnitureInfo {
    private String id, name, type;
    private float posX, posY;

    public FurnitureInfo(String id, String name, float x, float y, String type) {
        this.id = id;
        this.name = name;
        this.posX = x;
        this.posY = y;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }
}