// package com.example.clinicapp.network;
package com.example.clinicapp.network;

import java.io.Serializable;

// FIX: Usunięto zagnieżdżoną, zduplikowaną klasę ClientHandler.
// Jej prawidłowa wersja znajduje się w pakiecie com.example.clinicapp.server
public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final Object payload;

    public NetworkMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() { return type; }
    public Object getPayload() { return payload; }
}