package com.project.techapp;

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
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.app.Fragment;


import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button bluetoothButton, deviceButton;
    MainActivity mainActivity;

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        deviceButton = (Button) findViewById(R.id.deviceButton);
        mainActivity = this;
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();           // Check si l'appareil possède le Bluetooth
                if (bluetoothAdapter == null)                                                       // L'appareil ne possède pas le Bluetooth
                    Toast.makeText(getApplicationContext(), "This device doesn't have Bluetooth",
                            Toast.LENGTH_SHORT).show();
                else                                                                                // L'appareil possède le Bluetooth
                    if (!bluetoothAdapter.isEnabled()) {                                            // Check si le Bluetooth est activé ou non
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // Demande l'autorisation d'activé le Bluetooth
                        startActivityForResult(enableBtIntent, 1);
                }
            }
        });

        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                Set<BluetoothDevice> devices;
                devices = bluetoothAdapter.getBondedDevices();                                      // Récupère les appareil apairés
                String[] bankNames = new String[devices.size()];
                ArrayList<String> listNames = new ArrayList<>();
                for (BluetoothDevice blueDevice : devices) {                                        // Affiche les appareils appairés
                    Toast.makeText(MainActivity.this, "Device = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
                    listNames.add(blueDevice.getName()+"\n");
                }


                //Getting the instance of Spinner and applying OnItemSelectedListener on it
                mListView = (ListView) findViewById(R.id.deviceList);
                mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                //Creating the ArrayAdapter instance having the bank name list
                ArrayAdapter aa = new ArrayAdapter(mainActivity,android.R.layout.simple_spinner_item,listNames);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                mListView.setAdapter(aa);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Intent enableBtIntent = new Intent(this,Main2Activity.class);
                startActivity(enableBtIntent);
            }
        }
    }
}