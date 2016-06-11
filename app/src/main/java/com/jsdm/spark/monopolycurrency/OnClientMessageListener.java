package com.jsdm.spark.monopolycurrency;

/**
 * Created by Silvio on 6/10/2016.
 */
public interface OnClientMessageListener {
    void onMessage(ClientMonopolyMessage msg);
}
