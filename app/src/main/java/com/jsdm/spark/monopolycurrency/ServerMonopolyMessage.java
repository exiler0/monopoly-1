package com.jsdm.spark.monopolycurrency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Silvio on 6/5/2016.
 */
public class ServerMonopolyMessage implements Serializable {
    public String playerList;
    public String toLog;

    public ServerMonopolyMessage(GamePlayer[] gamePlayers, String toLog) {
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
