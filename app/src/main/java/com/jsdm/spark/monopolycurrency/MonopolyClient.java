package com.jsdm.spark.monopolycurrency;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Silvio on 6/5/2016.
 */
public class MonopolyClient {

    private Thread clientThread;
    private Socket socket;
    private volatile boolean running;

    public MonopolyClient(final String address, final int port) {

        running = true;

        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Trying to Connect", address + ":" + Integer.toString(port));
                    socket = new Socket(address, port);
                    Log.d("SeverConnect", socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort()));
                    while (running) {

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
