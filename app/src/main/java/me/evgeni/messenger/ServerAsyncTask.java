package me.evgeni.messenger;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class ServerAsyncTask extends AsyncTask {
    private Context context;
    private String text;
    private static final String TAG = "ServerAsyncTask";

    public ServerAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute");
    }

    @Override
    public String doInBackground(Object[] params) {
        try {
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */

            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();

            InputStream inputstream = client.getInputStream();
            text = convertStreamToString(inputstream);
            serverSocket.close();
            return text;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }


    @Override
    public void onPostExecute(Object result) {
        Log.d(TAG, "onPostExecute");
        String str = (String) result;
        if (str != null) {
            Log.d(TAG, "Message received - " + str);
            new AlertDialog.Builder(context).setMessage(str).show();
            Toast.makeText(context, "Message received - " + str, Toast.LENGTH_SHORT).show();
        }
    }

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
