package com.edisoninteractive.inrideads.Bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by mdumik on 15.01.2018.
 */

public interface DeviceConnectionListener {

    void onDeviceConnected(BluetoothDevice device);

    void onMessageReceived(String msg);

    void onDeviceReadyToWrite(BluetoothServerConnection.WritingThread writingThread);

    void onDeviceDisconnected();
}
