package com.project.pupitre;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
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

C'est super

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
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        setSupportActionBar(toolbar);


        // When clicking "Connection"
        // Est appelé lors de l'appui sur le bouton connexion
        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if bluetooth is enabled before starting connexion
                // Vérification de l'activation du bluetooth
                if(mBtAdapter.isEnabled()){
                    // Bluetooth is enabled, starting DeviceListActivity
                    // Si le Bluetooth est actif, démarrage de l'activité suivante
                    Intent DeviceListIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    // Start the DeviceListActivity
                    // Lance l'activité DeviceListActivity
                    startActivity(DeviceListIntent);
                }else{
                    // Bluetooth is disabled, notify user to Bluetooth on
                    // Le Bluetooth est désactivé, notifie l'utilisateur d'activé le Bluetooth
                    Toast.makeText(getApplicationContext(), R.string.bluetooth_must_be_turned_on, Toast.LENGTH_SHORT).show();
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

    // Detects clicks on menu item
    // Détecte les clicks sur les objets du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id){
            // Turn Bluetooth On/Off is clicked
            // Active/désactive le Bluetooth
            case R.id.bluetoothOnOff:
                enableDisableBT();
                break;
            // Make discoverable is clicked
            // Rend l'appareil visible
            case R.id.makeDiscoverable:
                makeDiscoverable();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Turn on/off the Bluetooth depending on the actual state
    // Active/désactive le Bluetooth en fonction de l'état actuel
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

    // Make the device discoverable by other bluetooth devices for 5 minutes
    // Rend l'appareil visible par les autres appareils visible pour 5 minutes
    public void makeDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }


    // Detect changes on Bluetooth
    // Détecte les changements sur le Bluetooth
    private final BroadcastReceiver mBroadcastReceiverBluetoothOnOff = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(mBtAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBtAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Notify the user that Bluetooth has been disabled
                        // Notifie l'utlilisateur que le Bluetooth à été désactivé
                        Toast.makeText(getApplicationContext(), R.string.bluetooth_disable, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Notify the user that Bluetooth has been enabled
                        // Notifie l'utlilisateur que le Bluetooth à été activé
                        Toast.makeText(getApplicationContext(), R.string.bluetooth_enable, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };
}
