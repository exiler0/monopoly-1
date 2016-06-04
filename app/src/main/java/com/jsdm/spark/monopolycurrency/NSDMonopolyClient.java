package com.jsdm.spark.monopolycurrency;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/**
 * Created by Silvio on 6/4/2016.
 */
public class NSDMonopolyClient {
    NsdManager nsdManager;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.ResolveListener resolveListener;
    private boolean stoppped = false;
    private NewGame.ConnectListener connectListener;

    public NSDMonopolyClient(Context context, NewGame.ConnectListener connectListener) {
        this.connectListener = connectListener;
        discoverService(context);
    }

    public void stopService() {
        if (stoppped) {
            return;
        }
        nsdManager.stopServiceDiscovery(discoveryListener);
        stoppped = true;
    }

    public void discoverService(final Context context) {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d("onResolveFailed", "Service Name: " + serviceInfo.getServiceName());
                Log.d("onResolveFailed", "Service Type: " + serviceInfo.getServiceType());
                Log.d("onResolveFailed", "Error Code: " + Integer.toString(errorCode));
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d("onServiceResolved", "Service Name: " + serviceInfo.getServiceName());
                Log.d("onServiceResolved", "Service Type: " + serviceInfo.getServiceType());
                Log.d("onServiceResolved", "Service Host: " + serviceInfo.getHost());
                Log.d("onServiceResolved", "Service Port: " + serviceInfo.getPort());

                // connect to server
                connectListener.onConnect();
            }
        };

        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d("onStartDiscoveryFailed", "Service Type: " + serviceType);
                Log.d("onStartDiscoveryFailed", "Error Code: " + Integer.toString(errorCode));
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d("onStopDiscoveryFailed", "Service Type: " + serviceType);
                Log.d("onStopDiscoveryFailed", "Error Code: " + Integer.toString(errorCode));
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d("onDiscoveryStarted", "Service Type: " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d("onDiscoveryStopped", "Service Type: " + serviceType);
                stoppped = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d("onServiceFound", "Service Name: " + serviceInfo.getServiceName());
                Log.d("onServiceFound", "Service Type: " + serviceInfo.getServiceType());

                if (serviceInfo.getServiceType().equals(NSDMonopolyServer.HTTP_TCP) && serviceInfo.getServiceName().equals(NSDMonopolyServer.NSD_MONOPOLY)) {
                    nsdManager.resolveService(serviceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d("onServiceLost", "Service Name: " + serviceInfo.getServiceName());
                Log.d("onServiceLost", "Service Type: " + serviceInfo.getServiceType());
                Log.d("onServiceLost", "Service Host: " + serviceInfo.getHost());
                Log.d("onServiceLost", "Service Port: " + Integer.toString(serviceInfo.getPort()));
                stoppped = true;
            }
        };

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        nsdManager.discoverServices(
                NSDMonopolyServer.HTTP_TCP, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    }
}
