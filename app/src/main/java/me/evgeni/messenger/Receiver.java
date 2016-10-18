package me.evgeni.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Evgheni on 10/12/2016.
 */

public class Receiver extends BroadcastReceiver {

    public static final String TAG = "receiver";

    private boolean discovering;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener listListener;
    private WifiP2pManager.ConnectionInfoListener connectionListener;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     */
    public Receiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                    WifiP2pManager.PeerListListener listener,
                    WifiP2pManager.ConnectionInfoListener connectionInfoListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.listListener = listener;
        this.connectionListener = connectionInfoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "action state changed");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "wifip2p enabled");
            } else {
                Log.d(TAG, "wifip2p disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                Log.d(TAG, "ACTION peers changed: manager not null, requesting peers");
                manager.requestPeers(channel, listListener);
            } else {
                Log.d(TAG, "ACTION peers changed: manager null");
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            manager.requestConnectionInfo(channel, connectionListener);
            Log.d(TAG, "action connection changed");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "action this device changed");
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "!!! discovery changed");
            discovering =! discovering;
            Log.d(TAG, String.valueOf(discovering));
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, String.valueOf(state));
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                Log.d(TAG, "... discovery started");
            } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                Log.d(TAG, "... discovery ended");
            }
        }
    }
}
