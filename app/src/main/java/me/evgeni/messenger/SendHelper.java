package me.evgeni.messenger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class SendHelper {
    static void sendMessage(final String host, final int port, final String message) {
        Runnable sendMessageRunnable = new Runnable() {
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
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        };
        new Thread(sendMessageRunnable).start();
    }
}
