package com.edgard.productsubscriber.model;


import com.edgard.productsubscriber.enuns.EventType;

public class Envelope {

    private EventType type;
    private String data;

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
