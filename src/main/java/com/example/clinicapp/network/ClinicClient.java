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
    private boolean isListening = false;

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

    public NetworkMessage sendMessageAndWaitForResponse(NetworkMessage msg, 
                                                      MessageType[] expectedResponseTypes, 
                                                      long timeoutMs) throws IOException, InterruptedException {
        if (!isConnected()) {
            throw new IOException("Brak połączenia z serwerem.");
        }

        // Start listening if not already listening
        if (!isListening) {
            startListening();
        }

        // Create a response holder
        final Object[] responseHolder = new Object[1];
        final Object lock = new Object();

        // Create a temporary listener for the response
        Consumer<NetworkMessage> originalListener = this.messageListener;

        this.messageListener = message -> {
            // Check if this is the response we're waiting for
            for (MessageType type : expectedResponseTypes) {
                if (message.getType() == type) {
                    synchronized (lock) {
                        responseHolder[0] = message;
                        lock.notify();
                    }
                    return;
                }
            }

            // If not the expected response and we have an original listener, pass it along
            if (originalListener != null) {
                originalListener.accept(message);
            }
        };

        // Send the message
        synchronized (out) {
            out.writeObject(msg);
            out.flush();
        }

        // Wait for the response with timeout
        synchronized (lock) {
            if (responseHolder[0] == null) {
                lock.wait(timeoutMs);
            }
        }

        // Restore the original listener
        this.messageListener = originalListener;

        // Return the response (may be null if timeout occurred)
        return (NetworkMessage) responseHolder[0];
    }

    public NetworkMessage sendMessageAndWaitForResponse(NetworkMessage msg, 
                                                      MessageType expectedResponseType, 
                                                      long timeoutMs) throws IOException, InterruptedException {
        return sendMessageAndWaitForResponse(msg, new MessageType[]{expectedResponseType}, timeoutMs);
    }

    public NetworkMessage sendMessageAndWaitForResponse(NetworkMessage msg, 
                                                      MessageType expectedResponseType) 
                                                      throws IOException, InterruptedException {
        return sendMessageAndWaitForResponse(msg, expectedResponseType, 5000); // 5 seconds default timeout
    }

    public void startListening() {
        if (isListening) {
            return; // Already listening, no need to start again
        }

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
                isListening = false; // Reset flag when listening stops
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                isListening = false; // Reset flag when listening stops due to error
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
        isListening = true; // Set flag to indicate listening has started
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            isListening = false; // Reset the listening flag when client is closed
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
