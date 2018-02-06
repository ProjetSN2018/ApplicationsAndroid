package com.project.xyz;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discovery;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(getApplicationContext(), "onReceive: STATE OFF", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(getApplicationContext(), "onReceive: STATE TURNING OFF", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(getApplicationContext(), "onReceive: STATE ON", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(getApplicationContext(), "onReceive: STATE TURNING ON", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);

                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(getApplicationContext(), "mBroadcasterReceiver2 : Discoverability Enabled.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "mBroadcasterReceiver2 : Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(getApplicationContext(), "mBroadcasterReceiver2 : Able to receive connections.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "mBroadcasterReceiver2 : Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(getApplicationContext(), "mBroadcasterReceiver2 : Not able to receive connections.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "mBroadcasterReceiver2 : Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(getApplicationContext(), "mBroadcasterReceiver2 : Connecting...", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "mBroadcasterReceiver2 : Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Toast.makeText(getApplicationContext(), "mBroadcasterReceiver2 : Connected.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "mBroadcasterReceiver2 : Connected.");
                        break;
                }
            }
        }
    };


    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive : ACTION FOUND.");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Toast.makeText(getApplicationContext(), "onReceive : "+ device.getName() + ": " + device.getAddress(), Toast.LENGTH_SHORT).show();
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    @Override
    protected void onDestroy(){
        Toast.makeText(getApplicationContext(), "onDestroy: called.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnOnOff = (Button) findViewById(R.id.btnOnOff);
        btnEnableDisable_Discovery = (Button) findViewById(R.id.btnDiscoverable);
        lvNewDevices = (ListView)findViewById(R.id.lvNewDevices);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnOnOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "onClick : enabling/disabling Bluetooth.");
                enableDisableBT();
            }
        });
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "enableDisableBT: Device doesn't have Bluetooth", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "enableDisableBT: Device doesn't have Bluetooth");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);

        }
    }

    public void btnEnableDisable_Discoverable(View view){
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");
        Toast.makeText(getApplicationContext(), "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.", Toast.LENGTH_SHORT).show();
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);


    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover : Looking for unpaired devices.");
        Toast.makeText(getApplicationContext(), "btnDiscover : Looking for unpaired devices.", Toast.LENGTH_SHORT).show();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "btnDiscover : Cancelling discovery.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "btnDiscover : Cancelling discovery.");
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    public void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_FINE LOCATION");
            if (permissionCheck !=0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
        }
    }
}
