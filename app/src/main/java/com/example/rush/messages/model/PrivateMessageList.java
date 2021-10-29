package com.example.rush.messages.model;

import java.util.ArrayList;

public class PrivateMessageList {
    Messages message;
    String key,otherUserName,otheruserId;

    public PrivateMessageList(String otherUserName, String otheruserId, Messages message, String key) {
        this.otheruserId = otheruserId;
        this.otherUserName = otherUserName;
        this.message = message;
        this.key = key;
    }

    public Messages getMessage() {
        return message;
    }

    public void setMessage(Messages message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public String getOtherUserName() {

        return otherUserName;
    }

    public String getOtheruserId() {
        return otheruserId;
    }

    public void setOtheruserId(String otheruserId) {
        this.otheruserId = otheruserId;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }
}
