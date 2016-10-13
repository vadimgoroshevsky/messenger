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


    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener listListener;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     */
    public Receiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pManager.PeerListListener listener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.listListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.

            Log.d(TAG, "action state changed");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // activity.setIsWifiP2pEnabled(true);

                Log.d(TAG, "wifip2p enabled");
            } else {
                // activity.setIsWifiP2pEnabled(false);

                Log.d(TAG, "wifip2p disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.

            if (manager != null) {
                Log.d(TAG, "manager not null, requesting peers");
                manager.requestPeers(channel, listListener);
            } else {
                Log.d(TAG, "manager null");
            }

            Log.d(TAG, "action peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
            Log.d(TAG, "action connection changed");

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
               //      .findFragmentById(R.id.frag_list);
            // fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                  //   WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            Log.d(TAG, "action this device changed");

        }
    }
}
