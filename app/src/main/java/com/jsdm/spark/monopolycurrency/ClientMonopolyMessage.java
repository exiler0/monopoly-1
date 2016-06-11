package com.jsdm.spark.monopolycurrency;

import java.io.Serializable;

/**
 * Created by Silvio on 6/10/2016.
 */
public class ClientMonopolyMessage implements Serializable {

    public String from;
    public String to;
    public int money;

    public ClientMonopolyMessage(String from, String to, int money) {
        this.from = from;
        this.to = to;
        this.money = money;
    }

    public String getPrintable() {
        String result = from +
                " -> " + String.valueOf(money) +
                " -> " + to;
        if (to.equals("Bank") || to.equals("Free Parking")) {
            result = from +
                    " <- " + String.valueOf(money) +
                    " <- " + to;
        }
        return result;
    }
}
