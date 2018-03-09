package com.project.xyz;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG ="MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discovery;

    BluetoothConnectionService mBluetoothConnection;

    Button btnSend;
    Button btnNextAct;
    EditText etMessage;
    BluetoothDevice mBTDevice;
    ListView lvNewDevices;
    MainActivity mainActivity;
    TextView tvState;

    private static final UUID INSECURE_UUID = UUID.fromString("a60a008b-049b-4e67-a9ea-b52a0c9207d7");

    public ArrayList<String> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBTDevicesList = new ArrayList<>();


    public MainActivity(){
        Log.d(TAG, "*************** Constructor : MainActivity() *****************************");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);

        Button btnOnOff = (Button) findViewById(R.id.btnOnOff);
        btnEnableDisable_Discovery = (Button) findViewById(R.id.btnDiscoverable);
        lvNewDevices = (ListView)findViewById(R.id.lvNewDevices);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnSend = (Button) findViewById(R.id.btnSend);
        btnNextAct = (Button) findViewById(R.id.btnNextAct);
        etMessage = (EditText) findViewById(R.id.etMessage);
        tvState = (TextView) findViewById(R.id.tvStatus);
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        btnOnOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                enableDisableBT();
            }
        });

        //Send string via Bluetooth
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "---- btnSend clicked ----");
                byte[] bytes = etMessage.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                etMessage.getText().clear();
            }
        });

        //Start the authentication activity
        btnNextAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "---- btnNextAct clicked ----");
                Intent authenticationIntent = new Intent(MainActivity.this, AuthenticationActivity.class);
                startActivity(authenticationIntent);
            }
        });

        btnEnableDisable_Discovery.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               btnEnableDisable_Discoverable(view);
               tvState.setText(R.string.str_tvConnected);
           }
        });
    }


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcasterReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcasterReceiver1: STATE TURNING OFF");
                        Toast.makeText(getApplicationContext(), "Bluetooth has been disabled.", Toast.LENGTH_SHORT).show();
                        mBTDevices.clear();
                        mBTDevicesList.clear();
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity,android.R.layout.simple_list_item_1,mBTDevices);
                        lvNewDevices.setAdapter(adapter);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcasterReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcasterReceiver1: STATE TURNING ON");
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
                        Log.d(TAG, "mBroadcasterReceiver2 : Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcasterReceiver2 : Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcasterReceiver2 : Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcasterReceiver2 : Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
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
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "onReceive : ACTION FOUND.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!mBTDevices.contains(device.getName()+"\n"+device.getAddress())) {
                    mBTDevices.add(device.getName()+"\n"+device.getAddress());
                    ArrayAdapter adapter = new ArrayAdapter(mainActivity,android.R.layout.simple_list_item_1,mBTDevices);
                    lvNewDevices.setAdapter(adapter);
                    mBTDevicesList.add(device);
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    mBTDevice = mDevice;
                }
                // Creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                }
                // Breaking a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }



    public void startConnection(){
        startBTConnection(mBTDevice,INSECURE_UUID);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        mBluetoothConnection.startClient(device,uuid);
    }

    //Turn on/off the Bluetooth
    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have Bluetooth capabilities.");
        }
        //If Bluetooth is enabled, disable it.
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        //If Bluetooth is disabled, enable it.
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    //Make the device visible to other devices
    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }

    //Discover nearby Bluetooth devices
    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        mBTDevices.clear();
        mBTDevicesList.clear();
        ArrayAdapter adapter = new ArrayAdapter(mainActivity,android.R.layout.simple_list_item_1,mBTDevices);
        lvNewDevices.setAdapter(adapter);

        //If the device is already discovering, stop the discovery
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        //If the device isn't discovering, start discovery
        if(!mBluetoothAdapter.isDiscovering()){
            //check BT permissions in manifest
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    //In order to discover nearby Bluetooth devices we need the location permission.
    //This function ask the user if he want to allow this application to use it's location.
    //Discovery can't run if the permission isn't granted.
    public void checkBTPermissions(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    public static int pow(int value){
        return value*value;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {

        //Toast.makeText(this,"BINGO",Toast.LENGTH_SHORT).show();


        //This function is called when the user click on a Bluetooth device in the listview.
        //First cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        //get the device name and address for the device clicked.
        final String deviceName = mBTDevices.get(i);
        String deviceAddress = mBTDevices.get(i);

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //Create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        //Open a dialog to confirm bonding with the clicked device.
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                            Log.d(TAG, "Trying to pair with " + deviceName);
                            Toast.makeText(getApplicationContext(), "Trying to pair with " + deviceName, Toast.LENGTH_SHORT).show();
                            mBTDevicesList.get(i).createBond();
                            Log.d(TAG, "Bonded with " + deviceName);
                            mBTDevice = mBTDevicesList.get(i);
                            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
                            Log.d(TAG, "New BluetoothConnection service");
                            startConnection();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage("Do you want to connect to this device :\n"+((TextView) view).getText()+" ?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }
}

