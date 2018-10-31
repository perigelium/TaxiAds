package com.edisoninteractive.inrideads.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.edisoninteractive.inrideads.R;

import java.util.ArrayList;

/**
 * Created by mdumik on 15.01.2018.
 */

public class ConnectedListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    public ConnectedListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public void refreshList(ArrayList<BluetoothDevice> devices) {
        if (null == devices) {
            mDevices = new ArrayList<>();
            notifyDataSetChanged();
        }
        this.mDevices = devices;
        notifyDataSetChanged();
    }
    public void addDeviceToList(BluetoothDevice device) {
        if (null == device) {
            return;
        }
        if (null == mDevices) {
            mDevices = new ArrayList<>();
        }
        if (!mDevices.contains(device)) {
            mDevices.add(device);
        }
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        if (null == mDevices || null == mDevices.get(position)) {
            return convertView;
        }
        final BluetoothDevice device = mDevices.get(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAdress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);

            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAdress != null) {
                deviceAdress.setText(device.getAddress());
            }
        }

        return convertView;
    }
}
