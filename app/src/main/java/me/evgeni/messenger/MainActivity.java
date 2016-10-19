package me.evgeni.messenger;

import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startService(new Intent(this, WifiDirectService.class));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Message message){
        Log.e(TAG, message.text);
        Log.e(TAG, message.type.name());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerEventBus();
        EventBus.getDefault().post(new Message("This is " + Build.MANUFACTURER + " " + Build.MODEL,
                Message.SenderType.DEFAULT));
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onResume");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onResume");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }
}