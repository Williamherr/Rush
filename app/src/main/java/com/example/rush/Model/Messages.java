package com.example.rush.Model;


public class Messages {

    private String name;
    private String uid;
    private String message;
    private com.google.firebase.Timestamp time;
    private String id;
    private String img;
    private Boolean isUrgent;

    public Messages(String name, String uid,String id, String message, com.google.firebase.Timestamp time) {
        this.name = name;
        this.id = id;
        this.uid = uid;
        this.message = message;
        this.time = time;
    }

    public Messages(String name, String uid,String id, String message, com.google.firebase.Timestamp time,String img, Boolean isUrgent) {
        this.name = name;
        this.id = id;
        this.uid = uid;
        this.message = message;
        this.time = time;
        this.img = img;
        this.isUrgent = isUrgent;
    }

    public Messages(){

    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public com.google.firebase.Timestamp getTime() {
        return time;
    }

    public void setTime(com.google.firebase.Timestamp time) {
        this.time = time;
    }
}
