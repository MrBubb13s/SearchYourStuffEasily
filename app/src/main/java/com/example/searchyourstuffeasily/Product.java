package com.example.searchyourstuffeasily;

//보관될 물품들의 정보를 클래스로 정의
public class Product {
    String id, name, locationInfo, placeLog;
    int count;

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
    public void setName(String name){
        this.name = name;
    }
    public void setCount(int count) {
        this.count = count;
    }   //upCount와 downCount의 역할은 setCount의 기능으로 폐합(일자:24/05/01)
    public void setLocationInfo(String info){
        this.locationInfo = info;
    }
    public void setPlaceLog(String log){
        this.placeLog = log;
    }
}