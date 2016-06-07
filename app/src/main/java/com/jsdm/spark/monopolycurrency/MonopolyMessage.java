package com.jsdm.spark.monopolycurrency;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Silvio on 6/5/2016.
 */
public class MonopolyMessage implements Serializable {
    public GamePlayer[] gamePlayers;
    public String trans_from;
    public String trans_to;
    public int value;
    public String toLog;

    public MonopolyMessage(GamePlayer[] gamePlayers, String trans_from, String trans_to, int value, String toLog) {

        this.gamePlayers = gamePlayers;
        this.trans_from = trans_from;
        this.trans_to = trans_to;
        this.value = value;
        this.toLog = toLog;
    }

    public String getPrintable() {
        String result = "";

        for (int i = 0; i < gamePlayers.length; i++) {
            result += gamePlayers[i].getPrintable() + "\n";
        }

        return result;
    }
}
