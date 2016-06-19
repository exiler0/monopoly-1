package com.jsdm.spark.monopolycurrency;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Silvio on 6/4/2016.
 */
public class NSDMonopolyServer {

    public static final String NSD_MONOPOLY = "NSDMonopoly";
    public static final String HTTP_TCP = "_http._tcp.";
    NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;
    ServerSocket server;
    List<ClientConnection> clientConnections;
    private final Thread serverThreadAccept;
    private volatile boolean running;
    private OnClientMessageListener onClientMessageListener;

    public NSDMonopolyServer(final Context context, final OnClientMessageListener onClientMessageListener) {
        this.onClientMessageListener = onClientMessageListener;
        try {
            server = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        running = true;
        clientConnections = Collections.synchronizedList(new ArrayList<ClientConnection>());
        registerService(server.getLocalPort(), context);

        serverThreadAccept = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        Socket socket = server.accept();
                        clientConnections.add(new ClientConnection(socket, onClientMessageListener));
                        Log.d("ClientConnect", socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThreadAccept.start();
    }

    public void sendToAll(Serializable msg) {
        for (ClientConnection cc : clientConnections) {
            cc.sendTo(msg);
            Log.d("Sending to clients", ((ServerMonopolyMessage) msg).getPrintable());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void stopService() {
        running = false;
        for (int i = 0; i < clientConnections.size(); i++) {
            clientConnections.get(i).stopService();
        }
        nsdManager.unregisterService(registrationListener);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void registerService(int port, Context context) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(NSD_MONOPOLY);
        serviceInfo.setServiceType(HTTP_TCP);
        serviceInfo.setPort(port);


        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d("onRegistrationFailed", "Name: " + serviceInfo.getServiceName());
                Log.d("onRegistrationFailed", "Error Code: " + Integer.toString(errorCode));
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d("onUnregistrationFailed", "Name: " + serviceInfo.getServiceName());
                Log.d("onUnregistrationFailed", "Error Code: " + Integer.toString(errorCode));
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d("onServiceRegistered", "Name: " + serviceInfo.getServiceName());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d("onServiceUnregistered", "Name: " + serviceInfo.getServiceName());
            }
        };

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    }

    private class ClientConnection {
        private final Thread clientThread;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket socket;
        private OnClientMessageListener onClientListener;
        private boolean clientRunning = true;

        public ClientConnection(Socket socket, OnClientMessageListener onClientMessageListener) {
            this.socket = socket;
            this.onClientListener = onClientMessageListener;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            clientThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            Log.d("Trying to read", "Client");
                            ClientMonopolyMessage msg = (ClientMonopolyMessage) in.readObject();
                            Log.d("Reading", msg.toString());
                            onClientListener.onMessage(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            clientThread.start();
        }

        private void sendTo(Serializable msg) {
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
            clientRunning = false;
        }
    }
}
