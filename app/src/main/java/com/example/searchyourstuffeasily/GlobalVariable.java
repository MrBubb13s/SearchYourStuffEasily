package com.example.searchyourstuffeasily;

import android.app.Application;

public class GlobalVariable extends Application {
    private String familyId, uId;

    public String getfamilyId() {
        return familyId;
    }
    public void setfamilyId(String id) {
        this.familyId = id;
    }
    public String getuId() {
        return uId;
    }
    public void setuId(String id) {
        this.uId = id;
    }
}
