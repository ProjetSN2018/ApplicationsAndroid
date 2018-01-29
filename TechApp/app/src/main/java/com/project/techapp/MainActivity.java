package com.project.techapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

public class MainActivity extends AppCompatActivity {

    Button bluetoothButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothButton = (Button) findViewById(R.id.bluetoothButton);

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null)
                    Toast.makeText(getApplicationContext(), "Pas de Bluetooth",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Avec Bluetooth",Toast.LENGTH_SHORT).show();
                    if (!bluetoothAdapter.isEnabled()) {
                         bluetoothAdapter.enable();
                }
            }
        });
    }
}
