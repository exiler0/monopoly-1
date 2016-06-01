package com.jsdm.spark.monopolycurrency;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NewGame extends AppCompatActivity {
    public final static String EXTRA_PLAYER_LIST = "com.spark.jsdm.monopolycurrency.playerlist";
    public final static String SEPARATOR = ": $";
    public final static int MAX_COUNT_PLAYERS = 15;
    public final static int MIN_COUNT_PLAYERS = 2;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        layout = (LinearLayout) findViewById(R.id.players_layout);
        layout.setVerticalScrollBarEnabled(true);
    }

    public void delPlayer(View view) {
        layout.removeView((View) view.getParent());
    }

    public void ready(View view) {

        if (!validateNamePlayers()) {
            Toast.makeText(getApplicationContext(), R.string.invalid_player, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateSeparatorPlayers()) {
            Toast.makeText(getApplicationContext(), R.string.invalid_name_player, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validatePlayersEquals()) {
            Toast.makeText(getApplicationContext(), R.string.invalid_equals, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validatePlayersCount()) {
            Toast.makeText(getApplicationContext(), R.string.invalid_count, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] players = getNameList();

        Intent intent = new Intent(this, PlayerList.class);
        intent.putExtra(EXTRA_PLAYER_LIST, players);
        startActivity(intent);
    }

    String[] getNameList() {
        String[] result = new String[layout.getChildCount()];

        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();

            result[i] = str;
        }

        return result;
    }

    boolean validatePlayersCount() {
        return (layout.getChildCount() >= MIN_COUNT_PLAYERS && layout.getChildCount() <= MAX_COUNT_PLAYERS);
    }

    boolean validatePlayersEquals() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            for (int j = 0; j < layout.getChildCount(); j++) {
                String str2 = ((EditText) ((LinearLayout) layout.getChildAt(j)).getChildAt(0)).getText().toString();
                if (i != j && str.equals(str2)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean validateNamePlayers() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            if (str.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    boolean validateSeparatorPlayers() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            if (str.contains(SEPARATOR)) {
                return false;
            }
        }
        return true;
    }

    public void addPlayer(View view) {
        if (layout.getChildCount() >= MAX_COUNT_PLAYERS) {
            Toast.makeText(getApplicationContext(), R.string.message_count_player, Toast.LENGTH_SHORT).show();
            return;
        }
        getLayoutInflater().inflate(R.layout.layout_player, layout);
    }
}

// TODO: Broadcast service
// TODO: Auto focus when adding new player
// TODO: Validation must be done by individual elements