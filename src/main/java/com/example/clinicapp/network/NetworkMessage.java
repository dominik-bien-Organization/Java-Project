package com.example.clinicapp.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessageType type;
    private final Object payload;

    public NetworkMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return this.type;
    }

    public Object getPayload() {
        return this.payload;
    }
}