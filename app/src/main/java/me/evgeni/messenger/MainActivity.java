package me.evgeni.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements WifiP2pManager.PeerListListener,
                    WifiP2pManager.ConnectionInfoListener,
                    AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private DeviceAdapter adapter;
    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.go) Button send;
    @BindView(R.id.input) EditText input;
    @BindView(R.id.layout) LinearLayout chatLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

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
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        send.setVisibility(View.GONE); input.setVisibility(View.GONE);
        mReceiver = new Receiver(mManager, mChannel, this, this);
        registerReceiver(mReceiver, intentFilter);
        adapter = new DeviceAdapter();
        listView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        adapter.setData(new ArrayList<>(peerList.getDeviceList()));
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            final WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(final String host, final int port, final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
                    InputStream inputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
                    while ((len = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (Exception e) { }
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        try {
            final String host = info.groupOwnerAddress.getHostAddress();
            if (info.groupFormed && info.isGroupOwner ) {
                Log.d(TAG, "I AM A SERVER!");
                // serverTask.execute();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                ServerSocket serverSocket = new ServerSocket(8888);
                                Socket client = serverSocket.accept();
                                final String clientAddress = client.getInetAddress().getHostAddress();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        input.setVisibility(View.VISIBLE); send.setVisibility(View.VISIBLE);
                                        send.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                            if (!input.getText().toString().equals("")) {
                                                sendMessage(clientAddress, 7777, input.getText().toString());
                                                final TextView textView = new TextView(getApplicationContext());
                                                textView.setBackgroundColor(Color.parseColor("#ffa500"));
                                                textView.setGravity(Gravity.END);
                                                textView.setGravity(Gravity.RIGHT);
                                                textView.setPadding(4, 4, 4, 4);
                                                textView.setText(input.getText().toString());
                                                if (chatLayout.getChildCount() > 10) chatLayout.removeViewAt(0);
                                                chatLayout.addView(textView);
                                                input.setText("");
                                            } else {
                                                new AlertDialog.Builder(getApplicationContext()).setMessage("Message can't be empty!").create().show();
                                            }
                                            }
                                        });
                                    }
                                });

                                InputStream inputstream = client.getInputStream();
                                final String message = convertStreamToString(inputstream);
                                Log.d(TAG, "message: " + message);
                                serverSocket.close();
                                client.close();

                                if (!message.equals("")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                            final TextView textView = new TextView(getApplicationContext());
                                            textView.setText(message);
                                            textView.setPadding(4, 4, 4, 4);
                                            if (chatLayout.getChildCount() > 10) chatLayout.removeViewAt(0);
                                            chatLayout.addView(textView);
                                        }
                                    });
                                }
                            }
                        } catch (IOException e) { }
                    }
                });

            } else if (info.groupFormed) {
                Log.d(TAG, "I AM A CLIENT!");
                send.setVisibility(View.VISIBLE); input.setVisibility(View.VISIBLE);
                sendMessage(host, 8888, "");
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!input.getText().toString().equals("")) {
                            sendMessage(host, 8888, input.getText().toString());
                            final TextView textView = new TextView(getApplicationContext());
                            textView.setBackgroundColor(Color.parseColor("#ffa500"));
                            textView.setGravity(Gravity.END);
                            textView.setGravity(Gravity.RIGHT);
                            textView.setPadding(4, 4, 4, 4);
                            textView.setText(input.getText().toString());
                            if (chatLayout.getChildCount() > 10) chatLayout.removeViewAt(0);
                            chatLayout.addView(textView);
                            input.setText("");
                        } else {
                            new AlertDialog.Builder(getApplicationContext()).setMessage("Message can't be empty!").create().show();
                        }
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(true) {
                                Log.d(TAG, "running server socket runnable on client");
                                ServerSocket serverSocket = new ServerSocket(7777);
                                Socket client = serverSocket.accept();
                                InputStream inputstream = client.getInputStream();
                                final String message = convertStreamToString(inputstream);
                                serverSocket.close();
                                client.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                        final TextView textView = new TextView(getApplicationContext());
                                        textView.setText(message);
                                        textView.setPadding(4, 4, 4, 4);
                                        if (chatLayout.getChildCount() > 10) chatLayout.removeViewAt(0);
                                        chatLayout.addView(textView);
                                    }
                                });
                            }
                        } catch (IOException e) { }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}