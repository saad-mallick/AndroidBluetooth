package com.example.saad.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final UUID SECUREUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothAdapter bluetoothAdapter;
    BluetoothConnection bluetoothConnection;
    ArrayList<BluetoothDevice> bluetoothDevices;
    ArrayAdapter<String> adapter;
    ListView listView;
    BluetoothDevice pairedDevice;

    //boolean clicked = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);
                adapter.add(device.getName());

                System.out.println(bluetoothDevices);
                System.out.println(adapter);
            }
        }
    };

    /*
    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = (BluetoothDevice) bluetoothAdapter.getBondedDevices().toArray()[0];
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                try {
                    if(!clicked) {
                        bluetoothConnection.startClient(device, SECUREUUID);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothDevices = new ArrayList<>();
        bluetoothConnection = new BluetoothConnection(MainActivity.this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        //IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        //registerReceiver(broadcastReceiver2, filter2);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetooth();
        setDeviceDiscoverable();

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, R.layout.textbox);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    bluetoothAdapter.cancelDiscovery();
                    pairedDevice = bluetoothDevices.get(position);
                    System.out.println("CONNECT TO THIS DEVICE: " + pairedDevice);
                    pairedDevice.createBond();
                    bluetoothConnection.startAccept(pairedDevice);
                }
            }
        });

        Button discover = findViewById(R.id.testbutton);
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });

        Button connect = findViewById(R.id.testbutton2);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice bonded = (BluetoothDevice) bluetoothAdapter.getBondedDevices().toArray()[0];
                BluetoothDevice actual = bluetoothAdapter.getRemoteDevice(bonded.getAddress());
                bluetoothConnection.startClient(actual, SECUREUUID);
            }
        });

        final EditText editText = findViewById(R.id.editText);
        final Button send = findViewById(R.id.submit);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = editText.getText().toString().getBytes(Charset.defaultCharset());
                bluetoothConnection.write(bytes);
            }
        });
    }

    public void openConnection(){
        //startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    private void enableBluetooth(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }
    }


    public void setDeviceDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    public void discoverDevices(){
        checkBTPermissions();
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);
    }

    //Change this later, copied from tutorial
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        //unregisterReceiver(broadcastReceiver2);
    }
}
