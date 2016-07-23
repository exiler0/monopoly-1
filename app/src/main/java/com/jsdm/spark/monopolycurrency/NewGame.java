package com.jsdm.spark.monopolycurrency;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

public class NewGame extends AppCompatActivity {
    public final static String EXTRA_PLAYER_LIST = "com.spark.jsdm.monopolycurrency.playerlist";
    public final static String EXTRA_SERVER_ADDRESS = "com.spark.jsdm.monopolycurrency.serveraddress";
    public final static String EXTRA_SERVER_PORT = "com.spark.jsdm.monopolycurrency.serverport";
    public final static String SEPARATOR = ": $";
    public final static int MAX_COUNT_PLAYERS = 15;
    public final static int MIN_COUNT_PLAYERS = 2;
    LinearLayout layout;
    private Thread findServerThread = null;
    private boolean findingServer = false;
    private Handler handler;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        layout = (LinearLayout) findViewById(R.id.players_layout);
        layout.setVerticalScrollBarEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        handler = new Handler();
    }

    public void delPlayer(View view) {
        layout.removeView((View) view.getParent());
        updateReadyButton();
    }

    public void ready(View view) {

        int indexProblem = validateNamePlayers();
        if (indexProblem != -1) {
            Toast.makeText(getApplicationContext(), R.string.invalid_player, Toast.LENGTH_SHORT).show();
            ((LinearLayout) layout.getChildAt(indexProblem)).getChildAt(0).requestFocus();
            return;
        }

        indexProblem = validateSeparatorPlayers();
        if (indexProblem != -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_name_player).replace("__separator__", SEPARATOR), Toast.LENGTH_SHORT).show();
            ((LinearLayout) layout.getChildAt(indexProblem)).getChildAt(0).requestFocus();
            return;
        }

        indexProblem = validatePlayersEquals();
        if (indexProblem != -1) {
            Toast.makeText(getApplicationContext(), R.string.invalid_equals, Toast.LENGTH_SHORT).show();
            ((LinearLayout) layout.getChildAt(indexProblem)).getChildAt(0).requestFocus();
            return;
        }

        if (!validatePlayersCount()) {
            Toast.makeText(getApplicationContext(), R.string.invalid_count, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), R.string.trying_to_connect, Toast.LENGTH_SHORT).show();

            if (!findingServer) {
                beginToFindServer();
            }

            return;
        }

        String[] players = getNameList();

        Intent intent = new Intent(this, PlayerList.class);
        intent.putExtra(EXTRA_PLAYER_LIST, players);
        shutdownWifi();
        startActivity(intent);
    }

    private void beginToFindServer() {
        if (findingServer) {
            return;
        }
        if (findServerThread != null && findServerThread.getState() != Thread.State.TERMINATED) {
            try {
                findServerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        findServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                findServer();
            }
        });
        findingServer = true;
        progressBar.setVisibility(View.VISIBLE);
        findServerThread.start();
    }

    private void findServer() {
        final Intent intent = new Intent(NewGame.this, PlayerList.class);
        final int port = MonopolyServer.PORT;

        WifiConfiguration monopolyWifi;
        WifiManager wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        while (!wifiManager.isWifiEnabled()) {
            if (!findingServer) {
                return;
            }
        }

        int networkId = existMonopolyWifi(wifiManager);

        if (networkId == -1) {
            networkId = addMonopolyWifi(wifiManager);
        }
        wifiManager.enableNetwork(networkId, true);
        int address = getIpAddress(wifiManager);
        if (address == -1) {
            return;
        }
        final String serverAddress = getGatewayAddress(address);
        Log.e("CLIENT", "\naddress:" + addressToString(address) + "\n");
        Log.e("CLIENT", "\naddress:" + serverAddress + "\n");

        handler.post(new Runnable() {
            @Override
            public void run() {
                intent.putExtra(EXTRA_SERVER_ADDRESS, serverAddress);
                intent.putExtra(EXTRA_SERVER_PORT, port);
                startActivity(intent);
                stopFindingServer();
            }
        });
    }

    private String getGatewayAddress(int address) {
        long longAddress = address;
        if (longAddress < 0) {
            longAddress = 4294967296L + longAddress;
        }

        String result = Long.toString(longAddress & 0x000000FF) + "." + Long.toString((longAddress & 0x0000FF00) >> 8) + "." + Long.toString((longAddress & 0x00FF0000) >> 16) + "." + Long.toString(1);
        return result;
    }

    private String addressToString(int address) {
        long longAddress = address;
        if (longAddress < 0) {
            longAddress = 4294967296L + longAddress;
        }

        String result = Long.toString(longAddress & 0x000000FF) + "." + Long.toString((longAddress & 0x0000FF00) >> 8) + "." + Long.toString((longAddress & 0x00FF0000) >> 16) + "." + Long.toString((longAddress & 0xFF000000) >> 24);
        return result;
    }

    private int addMonopolyWifi(WifiManager wifiManager) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = MonopolyServer.MONOPOLY_CURRENCY_AP;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return wifiManager.addNetwork(config);
    }

    private int getIpAddress(WifiManager wifiManager) {
        while (wifiManager.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED) {
            if (!findingServer) {
                return -1;
            }
        }

        while (wifiManager.getConnectionInfo().getIpAddress() == 0) {
            if (!findingServer) {
                return -1;
            }
        }

        return wifiManager.getConnectionInfo().getIpAddress();
    }

    private int existMonopolyWifi(WifiManager wifiManager) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifi : configuredNetworks) {
            Log.e("CLIENT", "\nwifi:" + wifi.SSID + "\n");
            if (wifi.SSID.equals("\"" + MonopolyServer.MONOPOLY_CURRENCY_AP + "\"")) {
                return wifi.networkId;
            }
        }
        return -1;
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

    int validatePlayersEquals() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            for (int j = 0; j < layout.getChildCount(); j++) {
                String str2 = ((EditText) ((LinearLayout) layout.getChildAt(j)).getChildAt(0)).getText().toString();
                if (i != j && str.equals(str2)) {
                    return j;
                }
            }
        }
        return -1;
    }

    int validateNamePlayers() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            if (str.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    int validateSeparatorPlayers() {
        for (int i = 0; i < layout.getChildCount(); i++) {
            String str = ((EditText) ((LinearLayout) layout.getChildAt(i)).getChildAt(0)).getText().toString();
            if (str.contains(SEPARATOR)) {
                return i;
            }
        }
        return -1;
    }

    public void addPlayer(View view) {
        if (layout.getChildCount() >= MAX_COUNT_PLAYERS) {
            Toast.makeText(getApplicationContext(), R.string.message_count_player, Toast.LENGTH_SHORT).show();
            return;
        }
        getLayoutInflater().inflate(R.layout.layout_player, layout);
        ((LinearLayout) layout.getChildAt(layout.getChildCount() - 1)).getChildAt(0).requestFocus();
        updateReadyButton();
    }

    private void updateReadyButton() {
        Button button = (Button) findViewById(R.id.readyButton);
        if (layout.getChildCount() > 0) {
            button.setText(getString(R.string.create_button));
        } else {
            button.setText(getString(R.string.join_button));
        }
    }

    private void shutdownWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (findingServer) {
            stopFindingServer();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.game_lost)
                .setTitle(R.string.sure)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishApp();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void stopFindingServer() {
        findingServer = false;
        try {
            findServerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(View.GONE);
    }

    public void finishApp() {
        shutdownWifi();
        super.onBackPressed();
    }
}
