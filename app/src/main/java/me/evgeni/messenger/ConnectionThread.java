package me.evgeni.messenger;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class ConnectionThread extends Thread {
    private static final String TAG = "ConnectionThread";
    private WifiP2pInfo info;
    private final int CONNECTION_PORT = 8888;
    private ServerSocket serverSocket;
    private Socket client;
    private String clientAddress, hostAddress;

    ConnectionThread(WifiP2pInfo info) {
        this.info = info;
    }

    @Override
    public void run() {
        try {
            hostAddress = info.groupOwnerAddress.getHostAddress();
            if (info.groupFormed) {
                while (true) {
                    serverSocket = new ServerSocket(CONNECTION_PORT);
                    client = serverSocket.accept();
                    clientAddress = client.getInetAddress().getHostAddress();

                    if (info.isGroupOwner)
                        SendHelper.sendMessage(clientAddress, CONNECTION_PORT, "SERVER MESSAGE");
                    else SendHelper.sendMessage(hostAddress, CONNECTION_PORT, "CLIENT MESSAGE");

                    InputStream inputstream = client.getInputStream();
                    String message = Helper.convertStreamToString(inputstream);
                    Log.d(TAG, "message: " + message);
                    serverSocket.close();
                    client.close();

                    if (!message.equals("")) {
                        EventBus.getDefault().post(new Message(message,
                                ((info.isGroupOwner) ? Message.SenderType.HOST : Message.SenderType.CLIENT)));
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Message message) {
        if (info.isGroupOwner)
            SendHelper.sendMessage(clientAddress, CONNECTION_PORT, message.text);
        else SendHelper.sendMessage(hostAddress, CONNECTION_PORT, message.text);
    }
}