package com.example.searchyourstuffeasily;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId, roomName;
    private ArrayList<Furniture> furnitureList = new ArrayList<Furniture>();

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    }
    public Room(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }
    public String getRoomName() {
        return roomName;
    }
    public ArrayList<Furniture> getFurnitureList() {
        return furnitureList;
    }
    public Furniture getFurnitureById(String id){
        for(int i = 0; i < furnitureList.size(); i++){
            Furniture result = furnitureList.get(i);
            String str = result.getId();

            if(str.equals(id))
                return result;
        }
        return null;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public void setFurnitureList(ArrayList<Furniture> fList) {
        this.furnitureList = fList;
    }

    public void addRoom(Furniture f){
        furnitureList.add(f);
    }
}