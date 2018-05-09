package com.project.pupitre;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";

    Button btnConnexion;
    Toolbar toolbar;
    BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////////////////////////////////////////////
        btnConnexion = (Button) findViewById(R.id.btn_connexion);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //When clicking "Connection"
        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Connection clicked");

                //Check if bluetooth is enabled before starting connexion
                if(mBtAdapter.isEnabled()){
                    //Bluetooth is enabled, starting DeviceListActivity
                    Intent DeviceListIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivity(DeviceListIntent);
                }else{
                    //Bluetooth is disabled
                    Toast.makeText(getApplicationContext(), "Bluetooth must be turned on.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Displays menu created in menu_main.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //Detects clicks on menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id){
            //Turn Bluetooth On/Off is clicked
            case R.id.bluetoothOnOff:
                enableDisableBT();
                break;
            //Make discoverable is clicked
            case R.id.makeDiscoverable:
                makeDiscoverable();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Turn on/off the Bluetooth depending on the actual state
    public void enableDisableBT(){
        if(mBtAdapter == null){
        }
        //If Bluetooth is enabled, disable it.
        if(!mBtAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiverBluetoothOnOff, BTIntent);
        }
        //If Bluetooth is disabled, enable it.
        if(mBtAdapter.isEnabled()){
            mBtAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiverBluetoothOnOff, BTIntent);
        }
    }

    //Make the device discoverable by other bluetooth devices for 5 mins
    public void makeDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private final BroadcastReceiver mBroadcastReceiverBluetoothOnOff = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(mBtAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBtAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(getApplicationContext(), "Bluetooth has been disabled.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(getApplicationContext(), "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
}
