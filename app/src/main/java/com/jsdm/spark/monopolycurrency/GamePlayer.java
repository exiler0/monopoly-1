package com.jsdm.spark.monopolycurrency;

/**
 * Created by Silvio on 9/20/2015.
 */
public class GamePlayer {
    int money;
    String name;

    GamePlayer(int money, String name) {
        this.money = money;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public boolean doPay(int money, GamePlayer player) {
        if (money > this.money) {
            return false;
        }
        player.money += money;
        this.money -= money;
        return true;
    }

    public boolean doBuy(int money) {
        if (money > this.money) {
            return false;
        }
        this.money -= money;
        return true;
    }

    public void doGet(int money) {
        this.money += money;
    }

    public void doGet(GamePlayer tax_player) {
        this.money += tax_player.money;
        tax_player.money = 0;
    }
}
