package com.jsdm.spark.monopolycurrency;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Silvio on 6/4/2016.
 */
public class MonopolyServer {
    public static final int PORT = 50007;
    public static final String MONOPOLY_CURRENCY_AP = "MonopolyCurrencyAP";
    ServerSocket server;
    List<ClientConnection> clientConnections;
    private final Thread serverThreadAccept;
    private volatile boolean running;
    private Context context;

    public MonopolyServer(final Context context, final OnClientMessageListener onClientMessageListener) {
        this.context = context;
        startHotSpot(context);

        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        running = true;
        clientConnections = Collections.synchronizedList(new ArrayList<ClientConnection>());

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

    private void startHotSpot(Context context) {
        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = MONOPOLY_CURRENCY_AP;
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

            Method isWifiApEnabledMethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
            }
            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apState = (Integer) getWifiApStateMethod.invoke(wifiManager);
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }
    }

    public void sendToAll(Serializable msg) {
        for (ClientConnection cc : clientConnections) {
            cc.sendTo(msg);
            Log.d("Sending to clients", ((ServerMonopolyMessage) msg).getPrintable());
        }
    }

    public void stopServing() {
        stopHotSpot();
        running = false;
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopHotSpot() {
        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = "MonopolyCurrencyAP";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, false);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }

    }

    private class ClientConnection {
        private final Thread clientThread;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private OnClientMessageListener onClientListener;

        public ClientConnection(Socket socket, OnClientMessageListener onClientMessageListener) {
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
    }
}
