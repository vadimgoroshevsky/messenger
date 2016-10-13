package me.evgeni.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener,
        AdapterView.OnItemClickListener, WifiP2pManager.ConnectionInfoListener {

    private static final String TAG = "MainActivity";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private List peers = new ArrayList();
    private DeviceAdapter adapter;
    private ListView listView;
    private ServerAsyncTask serverTask;
    private String textMessage = "Vasea";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new DeviceAdapter();
        listView.setAdapter(adapter);
        serverTask = new ServerAsyncTask(this);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

               /* Map record = new HashMap();
                record.put("listenport", String.valueOf(3000));
                record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
                record.put("available", "visible");
                WifiP2pDnsSdServiceInfo serviceInfo =
                        WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);
                mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Service added", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Service failed", Toast.LENGTH_LONG).show();
                    }
                });*/

                //mManager.requestPeers(mChannel, MainActivity.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new Receiver(mManager, mChannel, this);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ArrayList<WifiP2pDevice> list = new ArrayList<>(peerList.getDeviceList());
        adapter.setData(list);
        listView.setOnItemClickListener(this);
        Toast.makeText(this, String.valueOf(list.size()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            final WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connect success!");
                    Toast.makeText(getBaseContext(), "Connect success!", Toast.LENGTH_LONG).show();
                    requestConnectionInfo();
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Connect failure!");
                    Toast.makeText(getBaseContext(), "Connect failure!", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String host, int port) {
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[1024];
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = new ByteArrayInputStream(textMessage.getBytes(StandardCharsets.UTF_8));
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

/**
 * Clean up any open sockets when done
 * transferring or if an exception occurred.
 */ finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        //catch logic
                    }
                }
            }
        }
    }

    private void requestConnectionInfo () {
        mManager.requestConnectionInfo(mChannel, this);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        try {
        final String host = info.groupOwnerAddress.getHostAddress();
        Log.d(TAG, "onConnectionInfoAvailable: " + host);
        if (info.groupFormed && info.isGroupOwner ) {
            serverTask.execute();
        } else if (info.groupFormed) {
            Log.d(TAG, "onConnectionInfoAvailable: device is client, connect to group owner: startSocketClient ");
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage(host, 8888);
                }
            });
        }}
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
