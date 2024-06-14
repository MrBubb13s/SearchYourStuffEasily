package com.example.searchyourstuffeasily;

import java.util.ArrayList;

//본래 클래스명은 Room이었으나 실제로는 가구들의 리스트로 방을 만드는 방식으로 이루어진 것 같아 클래스 명칭을 Room에서 Furniture로 변경(일자:24/05/05)
//자료의 통일성을 위해 FurnitureInfo와 병합처리, 기존의 FurnitureInfo는 삭제하였음. 이상이 생길 시 복구할 것(일자:24/06/14)
public class Furniture {
    private String id, name, type;
    private float posX, posY;
    ArrayList<Product> Plist = new ArrayList<Product>();

    public Furniture(String name){
        this.name = name;
    }
    public Furniture(String id, String name, float x, float y, String type){
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
    public ArrayList<Product> getPlist(){
        return Plist;
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
    public Product getProductByIndex(int index){
        return Plist.get(index);
    }
    public Product getProductByName(String name) {
        for (Product product : Plist) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        return null;
    }
    public Product getProductById(String id){
        for(int i = 0; i < Plist.size(); i++){
            Product pTemp = Plist.get(i);
            String str = pTemp.getId();

            if(str.equals(id))
                return pTemp;
        }
        return null;
    }

    public void addProduct(Product p){
        Plist.add(p);
    }
    //deleteProduct와 searchProduct의 기능을 하나로 통합(일자:24/05/04)
    public Product manageProduct(String name, int command){
        for(int i = 0; i < Plist.size(); i++){
            String str = Plist.get(i).getName();

            switch(command){
                case 1:             //기존의 deleteProduct를 대체함.
                    if(str.equals(name)){
                        Plist.remove(i);
                        return null;
                    }
                    break;
                case 2:             //기존의 searchProduct를 대체함.
                    if(str.equals(name)){
                        Product product = Plist.get(i);
                        return product;
                    }
            }
        }
        return null;
    }
    public void updateProduct(String id, String name, String placeDetail, int count){
        for(int i = 0; i < Plist.size(); i++){
            Product pTemp = Plist.get(i);
            String str = pTemp.getId();

            if(str.equals(id)){
                pTemp.setName(name);
                pTemp.setLocationInfo(placeDetail);
                pTemp.setCount(count);
            };
        }
    }
}