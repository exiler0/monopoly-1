package com.jsdm.spark.monopolycurrency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Silvio on 6/5/2016.
 */
public class MonopolyMessage implements Serializable {
    public String playerList;
    public String toLog;

    public MonopolyMessage(GamePlayer[] gamePlayers, String toLog) {
        playerList = "";
        for (int i = 0; i < gamePlayers.length; i++) {
            playerList += gamePlayers[i].getPrintable();
            if (i != gamePlayers.length - 1) {
                playerList += ",";
            }
        }
        this.toLog = toLog;
    }

    public String getPrintable() {

        return playerList;
    }
}
