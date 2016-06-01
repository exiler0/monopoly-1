package com.jsdm.spark.monopolycurrency;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Transaction extends AppCompatActivity {
    public final static String EXTRA_VALUE = "com.spark.jsdm.monopolycurrency.transactionvalue";
    public final static String EXTRA_FROM = "com.spark.jsdm.monopolycurrency.transactionname";
    public final static String EXTRA_TO = "com.spark.jsdm.monopolycurrency.transactionreceiver";
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        layout = (LinearLayout) findViewById(R.id.players_pay);

        Intent intent = getIntent();
        String player = intent.getStringExtra(PlayerList.EXTRA_SENDER_NAME);
        String[] players = intent.getStringArrayExtra(NewGame.EXTRA_PLAYER_LIST);

        fillPlayers(player, players);
    }

    public void buttonClick(View v) {
        Intent intent = new Intent();
        int value;
        try {
            value = Integer.decode(((EditText) findViewById(R.id.edit_quantity)).getText().toString());
        } catch (NumberFormatException e) {
            value = 0;
        }
        String from = ((TextView) findViewById(R.id.player_name)).getText().toString();
        String to = ((Button) v).getText().toString();

        intent.putExtra(EXTRA_VALUE, value);
        intent.putExtra(EXTRA_FROM, from);
        intent.putExtra(EXTRA_TO, to);
        setResult(RESULT_OK, intent);
        playSound(R.raw.transfer);
        finish();
    }

    private void fillPlayers(String player, String[] players) {
        ((TextView) findViewById(R.id.player_name)).setText(player);

        for (int i = 0; i < players.length; i++) {
            if (!player.equals(players[i])) {
                Button button = new Button(this);
                button.setText(players[i]);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonClick(v);
                    }
                });
                layout.addView(button);
            }
        }
    }

    private void playSound(int resource) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resource);
        mediaPlayer.start();
    }
}
