package com.edisoninteractive.inrideads.Fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.edisoninteractive.inrideads.Bluetooth.ConnectedListAdapter;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.R;

import java.util.ArrayList;

public class FragBindBluetoothDevice extends Fragment implements View.OnClickListener {

    private static final String TAG = "edison_inrideads";
    private static final String EDISON_SCREEN_NAME = "EdisonInteractive";

    Activity activity;
    private Communicator mCommunicator;

    private View view;
    private Button btnEnableDiscoverable, btnRefreshList, btnBack;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBTDevice;
    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private ConnectedListAdapter mConnectedListAdapter;
    private ListView lvConnectedDevices;

    public FragBindBluetoothDevice() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCommunicator = (Communicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_frag_bind_bluetooth_device, container, false);
        initializeVariables();
        setOnClickListeners();
        displayConnectedDevices();
        registerConnectionBroadcast();
        setDeviceName();
        enableDisableDiscoverable();

        return view;
    }

    private void initializeVariables() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnEnableDiscoverable = (Button) view.findViewById(R.id.btnDiscoverable_on_off);
        btnRefreshList = (Button) view.findViewById(R.id.btn_refresh_list);
        btnBack = (Button) view.findViewById(R.id.btn_back);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver(mBroadcastReceiver4, filter);

        lvConnectedDevices = (ListView) view.findViewById(R.id.lvConnectedDevices);
    }

    private void setOnClickListeners() {
        btnEnableDiscoverable.setOnClickListener(this);
        btnRefreshList.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void displayConnectedDevices() {
        ArrayList<BluetoothDevice> connectedList = null;

        BluetoothManager btManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        try {
            connectedList = (ArrayList<BluetoothDevice>)
                    btManager.getConnectedDevices(BluetoothGatt.GATT);
        } catch (NullPointerException ex) {
            return;
        }

        if (null != connectedList && connectedList.size() > 0) {

            if (lvConnectedDevices.getAdapter() != null) {
                ((ConnectedListAdapter) lvConnectedDevices.getAdapter()).refreshList(connectedList);
            } else {
                mConnectedListAdapter = new ConnectedListAdapter(activity, R.layout.connected_device_adapter_view, connectedList);
                lvConnectedDevices.setAdapter(mConnectedListAdapter);
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnDiscoverable_on_off:
                enableDisableDiscoverable();
                break;

            case R.id.btn_refresh_list:
                refresh();
                break;

            case R.id.btn_back:
                mCommunicator.replaceFragment(FragServiceMenu.class.getSimpleName(), new FragServiceMenu());
                break;
        }
    }

    private void setDeviceName() {

        if (null == mBluetoothAdapter) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        mBluetoothAdapter.setName(EDISON_SCREEN_NAME + " - " + GlobalConstants.UNIT_ID);
    }

    private void enableDisableDiscoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        activity.registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    private void refresh() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (null == mBluetoothAdapter) {
            return;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }


    /***************************** BROADCAST RECEIVERS: ******************************/

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        displayConnectedDevices();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = activity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += activity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
        try {
            activity.unregisterReceiver(mReceiver);
        } catch (Exception ex) {
            Log.d(TAG, "mReceiver was not registered, to be unregistered");
        }
        try {
            activity.unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception ex) {
            Log.d(TAG, "Broadcast 1 was not registered, to be unregistered");
        }
        try {
            activity.unregisterReceiver(mBroadcastReceiver2);
        } catch (Exception e) {
            Log.d(TAG, "Broadcast 2 was not registered, to be unregistered");
        }
        try {
            activity.unregisterReceiver(mBroadcastReceiver3);
        } catch (Exception ex) {
            Log.d(TAG, "Broadcast 3 was not registered, to be unregistered");
        }
        try {
            activity.unregisterReceiver(mBroadcastReceiver4);
        } catch (Exception e) {
            Log.d(TAG, "Broadcast 4 was not registered, to be unregistered");
        }
    }

    private void registerConnectionBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        activity.registerReceiver(mReceiver, filter);
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

    public void onDeviceConnected(final BluetoothDevice device) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String deviceMac = device.getAddress();
                final BluetoothDevice connectedDevice = mBluetoothAdapter.getRemoteDevice(deviceMac);

                if (lvConnectedDevices.getAdapter() != null) {
                    ((ConnectedListAdapter) lvConnectedDevices.getAdapter()).addDeviceToList(connectedDevice);
                } else {
                    ConnectedListAdapter adapter = new ConnectedListAdapter(activity, R.layout.connected_device_adapter_view,
                            new ArrayList<BluetoothDevice>());
                    adapter.addDeviceToList(connectedDevice);
                    lvConnectedDevices.setAdapter(adapter);
                }
            }
        });
    }

    public void onDeviceDisconnected() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (lvConnectedDevices.getAdapter() != null) {
                    ((ConnectedListAdapter) lvConnectedDevices.getAdapter()).refreshList(null);
                } else {
                    ConnectedListAdapter adapter = new ConnectedListAdapter(activity, R.layout.connected_device_adapter_view,
                            new ArrayList<BluetoothDevice>());
                    adapter.refreshList(null);
                    lvConnectedDevices.setAdapter(adapter);
                }
            }
        });
    }
}
