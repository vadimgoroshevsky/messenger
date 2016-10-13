package me.evgeni.messenger;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class DeviceAdapter extends BaseAdapter {
    private ArrayList<WifiP2pDevice> devices;
    private TextView deviceAddress, deviceName;

    void setData(ArrayList<WifiP2pDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (devices != null && !devices.isEmpty()) ? devices.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2,parent, false);

        WifiP2pDevice device = (WifiP2pDevice) getItem(position);
        deviceName = (TextView) convertView.findViewById(android.R.id.text1);
        deviceAddress = (TextView) convertView.findViewById(android.R.id.text2);
        deviceName.setText(device.deviceName);
        deviceAddress.setText(device.deviceAddress);

        return convertView;
    }
}
