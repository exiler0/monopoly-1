package com.jsdm.spark.monopolycurrency;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    MonopolyServer monopolyServer;
    private MonopolyClient monopolyClient;
    private MediaPlayer mediaPlayer;
    private ServerMonopolyMessage serverMonopolyMessage;
    private ClientMonopolyMessage clientMonopolyMessage;
    private boolean as_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
        layout = (LinearLayout) findViewById(R.id.players_list);
        log = (LinearLayout) findViewById(R.id.game_log);

        Intent intent = getIntent();
        String[] players = intent.getStringArrayExtra(NewGame.EXTRA_PLAYER_LIST);

        if (players == null) {
            as_client = true;
            Toast.makeText(getApplicationContext(), R.string.client_mode, Toast.LENGTH_SHORT).show();
            String address = intent.getStringExtra(NewGame.EXTRA_SERVER_ADDRESS);
            int port = intent.getIntExtra(NewGame.EXTRA_SERVER_PORT, 0);
            Toast.makeText(getApplicationContext(), getString(R.string.found_server).replace("__address__", address).replace("__port__", Integer.toString(port)), Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            monopolyClient = new MonopolyClient(address, port, new OnServerMessageListener() {
                @Override
                public void onMessage(ServerMonopolyMessage msg) {
                    PlayerList.this.setServerMonopolyMessage(msg);
                    Log.d("Display Msg", msg.toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Receiving from server", PlayerList.this.getServerMonopolyMessage().getPrintable());
                            doTransactionFake(PlayerList.this.getServerMonopolyMessage().toLog);
                            updateButtons(PlayerList.this.getServerMonopolyMessage().playerList);
                        }
                    });
                }
            });

            return;
        }
        as_client = false;
        createPlayers(players);
        tax_player = new GamePlayer(0, getString(R.string.freeparking_player));
        final Handler handler = new Handler();
        monopolyServer = new MonopolyServer(this, new OnClientMessageListener() {
            @Override
            public void onMessage(final ClientMonopolyMessage msg) {
                PlayerList.this.setClientMonopolyMessage(msg);
                Log.d("Display Msg", msg.toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Receiving from client", PlayerList.this.getClientMonopolyMessage().getPrintable());
                        if (msg.from.equals(MonopolyClient.REQUEST_PLAYER_LIST)) {
                            doTransaction(player_list[0].getName(), getString(R.string.bank_pays), 0);
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerList.this);
                        builder.setMessage(msg.getPrintable())
                                .setTitle(R.string.accept_transaction)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        doTransaction(msg.from, msg.to, msg.money);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.create().show();
                    }
                });

            }
        });
        Toast.makeText(getApplicationContext(), R.string.server_mode, Toast.LENGTH_SHORT).show();
    }

    private void updateButtons(String playerList) {
        String[] split = playerList.split(",");
        if (layout.getChildCount() != split.length) {
            inflateLayout(split);
        } else {
            updateLayout(split);
        }
    }

    private void updateLayout(String[] playerList) {
        for (int i = 0; i < playerList.length; i++) {
            Button button = (Button) layout.getChildAt(i);
            button.setText(playerList[i]);
        }
    }

    private void inflateLayout(final String[] playerList) {
        layout.removeAllViews();
        for (int i = 0; i < playerList.length; i++) {
            Button button = new Button(this);
            button.setText(playerList[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PlayerList.this, Transaction.class);
                    intent.putExtra(EXTRA_SENDER_NAME, getName(v));
                    intent.putExtra(NewGame.EXTRA_PLAYER_LIST, extractNames(playerList));
                    startActivityForResult(intent, 0);
                }
            });

            layout.addView(button);
        }
    }

    private String[] extractNames(String[] playerList) {
        String[] result = new String[playerList.length];

        for (int i = 0; i < playerList.length; i++) {
            result[i] = playerList[i].substring(0, playerList[i].indexOf(NewGame.SEPARATOR));
        }

        return result;
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

        if (as_client) {
            if (monopolyClient != null) {
                monopolyClient.sendToServer(new ClientMonopolyMessage(data.getStringExtra(Transaction.EXTRA_FROM), data.getStringExtra(Transaction.EXTRA_TO), data.getIntExtra(Transaction.EXTRA_VALUE, 0)));
            }
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
                return;
            }

            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            addToLog(result);
        } else if (to.equals(getString(R.string.bank_player))) {
            if (!player.doBuy(money)) {
                Toast.makeText(getApplicationContext(), R.string.cant_buy, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            addToLog(result);
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
                return;
            }

            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            addToLog(result);
        }

        monopolyServer.sendToAll(new ServerMonopolyMessage(player_list, result));
        refreshButtonsPlayers();
        playSound(R.raw.transfer);
    }

    private void doTransactionFake(String toLog) {
        playSound(R.raw.transfer);
        Toast.makeText(getApplicationContext(), toLog, Toast.LENGTH_SHORT).show();
        addToLog(toLog);
    }

    private void playSound(int resource) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, resource);
        mediaPlayer.start();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.state_lost)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishGame();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    public void finishGame() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        tryStopServer();
        tryStopClient();

        super.onDestroy();
    }

    private void tryStopClient() {
        if (monopolyClient != null) {
            monopolyClient.stopService();
        }
    }

    private void tryStopServer() {
        if (monopolyServer != null) {
            try {
                monopolyServer.stopServing();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setServerMonopolyMessage(ServerMonopolyMessage serverMonopolyMessage) {
        this.serverMonopolyMessage = serverMonopolyMessage;
    }

    public ServerMonopolyMessage getServerMonopolyMessage() {
        return serverMonopolyMessage;
    }

    public ClientMonopolyMessage getClientMonopolyMessage() {
        return clientMonopolyMessage;
    }

    public void setClientMonopolyMessage(ClientMonopolyMessage clientMonopolyMessage) {
        this.clientMonopolyMessage = clientMonopolyMessage;
    }
}
