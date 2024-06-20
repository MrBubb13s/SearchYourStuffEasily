package com.example.searchyourstuffeasily;

//보관될 물품들의 정보를 클래스로 정의
public class Product {
    private String id, name, roomName, furnitureName, locationInfo, imageUrl;
    private int count;

    //Product와 Food 구분을 분리해 생성자 개편(일자:24/05/05)
    public Product(String id, String name, String info){
        this.id = id;
        this.name = name;
        this.count = 1;             //물품 개수 미지정 시 기본 값으로 1개를 갖도록 지정
        this.locationInfo = info;
    }
    public Product(String id, String name, String info, int count){
        this.id = id;
        this.name = name;
        this.count = count;
        this.locationInfo = info;
    }

    public String getId() {
        return id;
    }
    public String getName(){
        return name;
    }
    public String getLocationInfo(){
        return locationInfo;
    }
    public int getCount(){
        return count;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getRoomName() {
        return roomName;
    }
    public String getFurnitureName() {
        return furnitureName;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setLocationInfo(String info){
        this.locationInfo = info;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public void setFurnitureName(String furnitureName){
        this.furnitureName = furnitureName;
    }
}