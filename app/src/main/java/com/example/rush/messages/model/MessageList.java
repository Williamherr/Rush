package com.example.rush.messages.model;

import java.util.ArrayList;

public class MessageList {
    Messages messages;
    String key;
    Members members;

    public MessageList() {

    }

    public MessageList(Members members, Messages messages, String key) {
        this.members = members;
        this.messages = messages;
        this.key = key;
    }



    public Messages getMessages() {
        return messages;
    }

    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMembers(Members members) {
        this.members = members;
    }

    public String getKey() {
        return key;
    }

    public Members getMembers() {
        return members;
    }
}
