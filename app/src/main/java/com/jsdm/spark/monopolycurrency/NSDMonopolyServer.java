package com.jsdm.spark.monopolycurrency;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/**
 * Created by Silvio on 6/4/2016.
 */
public class NSDMonopolyServer {


    public static final String NSD_MONOPOLY = "NSDMonopoly";
    public static final String HTTP_TCP = "_http._tcp.";
    NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;

    public NSDMonopolyServer(int port, Context context) {
        registerService(port, context);
    }

    public void stopService() {
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

}
