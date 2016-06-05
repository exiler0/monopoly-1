package com.jsdm.spark.monopolycurrency;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Created by Silvio on 6/5/2016.
 */
public class MonopolyClient {

    private Thread clientThread;
    private Socket socket;
    private volatile boolean running;
    private OnMsgListener onMsgListener;
    private ObjectInputStream in;

    public MonopolyClient(final String address, final int port, final OnMsgListener onMsgListener) {
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
                    while (running) {
                        try {
                            Log.d("Trying to read", "Client");
                            Object msg = in.readObject();
                            Log.d("Reading", msg.toString());
                            onMsgListener.onMsg(msg);
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

    public void stopService() {
        running = false;
    }
}
