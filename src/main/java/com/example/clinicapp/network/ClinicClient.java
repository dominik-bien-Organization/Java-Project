package com.example.clinicapp.network;

import com.example.clinicapp.util.AlertMessage;
import javafx.application.Platform;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;



public class ClinicClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<NetworkMessage> onMessageReceived;
    private Consumer<NetworkMessage> messageListener;

    public ClinicClient(String serverAddress, int port, Consumer<NetworkMessage> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
        this.   onMessageReceived = onMessageReceived;
    }


    public void setMessageListener(Consumer<NetworkMessage> listener) {
        this.messageListener = listener;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void sendMessage(NetworkMessage msg) throws IOException {
        if (!isConnected()) {
            throw new IOException("Brak połączenia z serwerem.");
        }

        synchronized (out) {
            out.writeObject(msg);
            out.flush();
        }
    }




    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Object obj = in.readObject();  // tutaj zmieniam na 'in'
                    if (messageListener != null && obj instanceof NetworkMessage) {
                        messageListener.accept((NetworkMessage) obj);
                    }
                }
            } catch (EOFException eof) {
                System.out.println("Strumień został zamknięty, kończę nasłuchiwanie.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public Socket getSocket() {
        return socket;
    }

    }
