package com.example.rush.messages.model;

import java.util.ArrayList;

public class allMessageList {


    ArrayList<MessageList> messages;

    public allMessageList(){
        messages = new ArrayList<>();
    }

    public allMessageList(ArrayList<MessageList> messages) {
        this.messages = messages;
    }

    public ArrayList<MessageList> getAllMessages() {
        return messages;
    }

    public MessageList getMessages(int index) {
        return messages.get(index);
    }

    public void addMessages(MessageList message) {
        this.messages.add(message);
    }
}
