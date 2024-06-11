package com.example.searchyourstuffeasily;

import java.util.ArrayList;
import java.util.List;
import com.example.searchyourstuffeasily.FurnitureInfo;
public class Room {
    private String roomId, roomName;
    private List<FurnitureInfo> furnitureList;

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
        this.furnitureList = new ArrayList<>();
    }
    public Room(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.furnitureList = new ArrayList<>();
    }

    public String getRoomId() {
        return roomId;
    }
    public String getRoomName() {
        return roomName;
    }
    public List<FurnitureInfo> getFurnitureList() {
        return furnitureList;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public void setFurnitureList(List<FurnitureInfo> furnitureList) {
        this.furnitureList = furnitureList;
    }

    public void addRoom(FurnitureInfo info){
        furnitureList.add(info);
    }
}