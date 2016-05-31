package com.jsdm.spark.monopolycurrency;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class PlayerList extends AppCompatActivity {
    public final static int INITIAL_MONEY = 1500;
    public final static String EXTRA_SENDER_NAME = "com.spark.jsdm.monopolycurrency.transactionsender";
    LinearLayout layout;
    LinearLayout log;
    GamePlayer[] player_list;
    GamePlayer tax_player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
        layout = (LinearLayout) findViewById(R.id.players_list);
        log = (LinearLayout) findViewById(R.id.game_log);

        Intent intent = getIntent();
        String[] players = intent.getStringArrayExtra(NewGame.EXTRA_PLAYER_LIST);

        createPlayers(players);
        tax_player = new GamePlayer(0, "Tax");
    }

    void createPlayers(String[] players) {
        player_list = new GamePlayer[players.length];
        for (int i = 0; i < players.length; i++) {
            player_list[i] = new GamePlayer(INITIAL_MONEY, players[i]);
            createButtonPlayer(player_list[i]);
        }
    }

    void createButtonPlayer(GamePlayer player) {
        Button button = new Button(this);
        button.setText(getTextPlayer(player));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayerList.this, Transaction.class);
                intent.putExtra(EXTRA_SENDER_NAME, getName(v));
                intent.putExtra(NewGame.EXTRA_PLAYER_LIST, getPlayersNames());
                startActivityForResult(intent, 0);
            }
        });

        layout.addView(button);
    }

    private String[] getPlayersNames() {
        String[] result = new String[player_list.length];

        for (int i = 0; i < player_list.length; i++) {
            result[i] = player_list[i].getName();
        }

        return result;
    }

    private String getTextPlayer(GamePlayer player) {
        return player.getName() + NewGame.SEPARATOR + String.valueOf(player.getMoney());
    }

    String getName(View v) {
        Button button = (Button) v;
        int index = button.getText().toString().indexOf(NewGame.SEPARATOR);
        return button.getText().toString().substring(0, index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        doTransaction(data.getStringExtra(Transaction.EXTRA_FROM), data.getStringExtra(Transaction.EXTRA_TO), data.getIntExtra(Transaction.EXTRA_VALUE, 0));
    }

    private void doTransaction(String from, String to, int money) {
        String result = from +
                " -> " + String.valueOf(money) +
                " -> " + to;

        GamePlayer player = getPlayerByName(from);

        if (player == null) {
            Toast.makeText(getApplicationContext(), R.string.error_in_transaction, Toast.LENGTH_SHORT).show();
            return;
        }

        if (to.equals(getString(R.string.freeparking_player))) {
            if (!player.doPay(money, tax_player)) {
                Toast.makeText(getApplicationContext(), R.string.cant_buy, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                addToLog(result);
            }
        } else if (to.equals(getString(R.string.bank_player))) {
            if (!player.doBuy(money)) {
                Toast.makeText(getApplicationContext(), R.string.cant_buy, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                addToLog(result);
            }
        } else if (to.equals(getString(R.string.bank_pays))) {
            result = from +
                    " <- " + String.valueOf(money) +
                    " <- " + to;
            player.doGet(money);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            addToLog(result);

        } else if (to.equals(getString(R.string.freeparking_pays))) {
            result = from +
                    " <- " + String.valueOf(tax_player.getMoney()) +
                    " <- " + to;
            player.doGet(tax_player);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            addToLog(result);
        } else {
            GamePlayer player_gets = getPlayerByName(to);

            if (player_gets == null) {
                Toast.makeText(getApplicationContext(), R.string.error_in_transaction, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!player.doPay(money, player_gets)) {
                Toast.makeText(getApplicationContext(), R.string.cant_buy, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                addToLog(result);
            }
        }

        refreshButtonsPlayers();
    }

    private void refreshButtonsPlayers() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            Button button = (Button) layout.getChildAt(i);
            GamePlayer player = getPlayerByName(getName(button));
            button.setText(getTextPlayer(player));
        }
    }

    private void addToLog(String transaction) {
        TextView entry = new TextView(this);
        entry.setText(transaction);
        log.addView(entry, 0);
    }

    private GamePlayer getPlayerByName(String name) {
        for (int i = 0; i < player_list.length; i++) {
            if (name.equals(player_list[i].getName())) {
                return player_list[i];
            }
        }
        return null;
    }
}

// TODO: sound on transactions
// TODO: Style buttons
// TODO: Save and Reload State