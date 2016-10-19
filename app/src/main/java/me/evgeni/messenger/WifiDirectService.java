package me.evgeni.messenger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class WifiDirectService extends Service implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener {

    private static final String TAG = "WifiDirectService";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private ConnectionThread connectionThread;
    private WifiP2pDevice currentDevice;

    @Override
    public void onCreate() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new Receiver(mManager, mChannel, this, this);
        registerReceiver(mReceiver, intentFilter);
        initializePeerDiscovery();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();
        initializePeerDiscovery();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(connectionThread);
        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d(TAG, "onPeersAvailable");
        List<WifiP2pDevice> deviceList = new ArrayList<>(peers.getDeviceList());
        if (!deviceList.isEmpty()) currentDevice = deviceList.get(0);
        if (currentDevice != null) connectToDevice(currentDevice);
    }

    private void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Connect success!");
                Toast.makeText(getBaseContext(), "Connect success!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Connect failure!");
                Toast.makeText(getBaseContext(), "Connect failure!", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "onConnectionInfoAvailable");
        if (info != null && info.groupOwnerAddress != null) {
            connectionThread = new ConnectionThread(info);
            if (!EventBus.getDefault().isRegistered(connectionThread))
                EventBus.getDefault().register(connectionThread);
            connectionThread.start();
        } else if (currentDevice != null) connectToDevice(currentDevice);
    }

    private void initializePeerDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery began", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Discovery failed", Toast.LENGTH_LONG).show();
            }
        });
    }

}
