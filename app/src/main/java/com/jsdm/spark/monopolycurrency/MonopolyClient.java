package com.jsdm.spark.monopolycurrency;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by Silvio on 6/5/2016.
 */
public class MonopolyClient {

    public static final String REQUEST_PLAYER_LIST = "REQUEST PLAYER LIST";
    private Thread clientThread;
    private Socket socket;
    private volatile boolean running;
    private OnServerMessageListener onMsgListener;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public MonopolyClient(final String address, final int port, final OnServerMessageListener onMsgListener) {
        this.onMsgListener = onMsgListener;

        running = true;

        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Trying to Connect", address + ":" + Integer.toString(port));
                    socket = new Socket(address, port);
                    Log.d("SeverConnect", socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort()));
                    in = new ObjectInputStream(socket.getInputStream());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    sendToServer(new ClientMonopolyMessage(REQUEST_PLAYER_LIST, " ", 0));
                    while (running) {
                        try {
                            Log.d("Trying to read", "Client");
                            ServerMonopolyMessage msg = (ServerMonopolyMessage) in.readObject();
                            Log.d("Reading", msg.toString());
                            onMsgListener.onMessage(msg);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.start();
    }

    void sendToServer(Serializable msg) {
        if (out == null) {
            Log.d("null out stream", "Trying to send data");
            return;
        }
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopService() {
        running = false;
    }
}
