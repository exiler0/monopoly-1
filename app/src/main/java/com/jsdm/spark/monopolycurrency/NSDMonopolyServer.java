package com.jsdm.spark.monopolycurrency;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.jsdm.spark.monopolycurrency.server.Client;
import com.jsdm.spark.monopolycurrency.server.Server;

import java.io.IOException;
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

    public NSDMonopolyServer(final Context context) {
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
                        clientConnections.add(new ClientConnection(socket));
                        Log.d("ClientConnect", socket.getInetAddress().toString() + ":" + Integer.toString(socket.getPort()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThreadAccept.start();
    }

    public void sendToAll(String msg) {
        for (ClientConnection cc : clientConnections) {
            cc.sendTo(msg);
        }
    }

    public void stopService() {
        running = false;
        nsdManager.unregisterService(registrationListener);
    }

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
        private ObjectOutputStream out;
        private Socket socket;

        public ClientConnection(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendTo(Object msg) {
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
