package com.edisoninteractive.inrideads.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;

/**
 * Created by mdumik on 18.01.2018.
 */

public class BluetoothService extends Service implements DeviceConnectionListener {

    private static final String TAG = "edison_inrideads";

    public static final int BLUETOOTH_MESSAGE_WHAT = 11;

    private IBinder mBinder = new MyBinder();

    private BluetoothAdapter mBluetoothAdapter;

    private static boolean isRunning;

    private BluetoothServerConnection bluetoothServerConnection;

    private BluetoothServerConnection.WritingThread writingThread;

    private Handler mHandler = null;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onBind() called");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onRebind() called");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onUnbind() called");
        return true;
    }

    public class MyBinder extends Binder {
       public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void onPaymentResult(boolean result) {

        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onPaymentResult() called");

        String resultMsg = result ? "success" : "failure";

        if (writingThread != null) {
            writingThread.sendMessage(resultMsg);
        } else {
            Log.d(GlobalConstants.APP_LOG_TAG, "WritingThread == null, CAN NOT SEND MESSAGE!");
        }
    }

    @Override
    public void onDeviceReadyToWrite(BluetoothServerConnection.WritingThread writingThread) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onDeviceReadyToWrite() called");
        this.writingThread = writingThread;
    }

    public void setHandler(Handler handler)
    {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: setHandler() called");
        mHandler = handler;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onStartCommand() called");

        if (isRunning) {
            return super.onStartCommand(intent, flags, startId);
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter) {
            return super.onStartCommand(intent, flags, startId);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStateReceiver, filter);
            mBluetoothAdapter.enable();
        } else {
            registerConnectionBroadcast();
            openServerSocket();
        }

        return START_STICKY;
    }

    /**
     * This Broadcast listens to device bluetooth state (on/off)
     */
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()) {
                registerConnectionBroadcast();
                openServerSocket();
            }
        }
    };

    private void registerConnectionBroadcast() {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: registerConnectionBroadcast() called");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * this Broadcast listening to connected/disconnected Devices
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "BROADCAST onReceive state = CONNECTED!");
                onDeviceConnected(device);
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d(TAG, "BROADCAST onReceive state = DISCONNECTED!");
                onDeviceDisconnected();
            }
        }
    };

    private void openServerSocket() {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: openServerSocket() called");

        isRunning = true;
        if (bluetoothServerConnection != null) {
            bluetoothServerConnection.stop();
        }
        bluetoothServerConnection = new BluetoothServerConnection(this, this);
        bluetoothServerConnection.start();
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onDeviceConnected() connected to " + device.getName());
    }

    @Override
    public void onDeviceDisconnected() {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onDeviceDisconnected() called");
        openServerSocket();
    }

    @Override
    public void onMessageReceived(String msg) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onMessageReceived() Message = " + msg);
        if (null != mHandler) {
            Message message = mHandler.obtainMessage(BLUETOOTH_MESSAGE_WHAT, msg);
            message.sendToTarget();
        } else {
            Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothService: onMessageReceived() " +
            "mHandler is null, can't show received message");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothService: onDestroy() called");

        if (mHandler != null)
        {
            mHandler = null;
        }

        try {
            unregisterReceiver(bluetoothStateReceiver);
        } catch (Exception ex) {

        }

        try {
            unregisterReceiver(mReceiver);
        } catch (Exception ex) {

        }

        isRunning = false;
    }
}
