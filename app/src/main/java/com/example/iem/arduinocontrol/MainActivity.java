package com.example.iem.arduinocontrol;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView tv_noconnect;
    Button butchange;
    LinearLayout bg;
    boolean basemode;
    BluetoothA2dp bt;
    //String deviceName = "Adafruit BlueFruit LE";
    String deviceName = "G4";
    BluetoothDevice arduino = null;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(MainActivity.class.getName(), "New Device = " + device.getName());
                if(device!=null){
                    if(device.getName() != null){
                        if(device.getName().equals(deviceName)){
                            arduino = device;
                            connectToArduino(BluetoothAdapter.getDefaultAdapter());
                        }
                    }

                }

            }
        }
    };
    private BluetoothSocket mSocket;

    Set<BluetoothDevice> listDevice;
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_noconnect = (TextView) findViewById(R.id.tv_noconnect);
        butchange = (Button) findViewById(R.id.butchangemod);
        bg = (LinearLayout) findViewById(R.id.ll_bg);
        basemode = true;
        bg.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        butchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(basemode){
                    basemode = false;
                    bg.setBackgroundColor(getResources().getColor(R.color.red));
                }else{
                    basemode = true;
                    bg.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }

            }
        });

        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        bluetoothConnect(bluetooth);

    }

    public boolean checkDeviceHasBluetooth(BluetoothAdapter bA){
        if(bA == null){
            Toast.makeText(MainActivity.this, "Votre smartphone n'a pas de Bluetooth", Toast.LENGTH_SHORT).show();
           return false;
        }else{
            return true;
        }
    }

    public void enableBluetooth(BluetoothAdapter bA){
        if (!bA.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

    }

    public Set<BluetoothDevice> getKnownDevices(BluetoothAdapter bA){
        Set<BluetoothDevice> devices;
        devices = bA.getBondedDevices();
        for (BluetoothDevice blueDevice : devices) {
            Log.d(MainActivity.class.getName(), "Device = " + blueDevice.getName());
        }
        return devices;
    }

    public void getUnknownDevices(BluetoothAdapter bA){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
        bA.startDiscovery();
    }

    public void findArduino(BluetoothAdapter bA){
        for(BluetoothDevice device : listDevice){
            if(device.getName().equals(deviceName)){
                arduino = device;
                connectToArduino(bA);
            }
        }

        if(arduino == null){
            getUnknownDevices(bA);
        }
    }

    public void connectToArduino(BluetoothAdapter bA){

//        Method connect = null;
//        try {
//            connect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//
//
//        bA.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
//            @Override
//            public void onServiceConnected(int profile, BluetoothProfile proxy) {
//                Log.d(MainActivity.class.getName(),"it works");
//                bt= (BluetoothA2dp)proxy;
//            }
//
//            @Override
//            public void onServiceDisconnected(int profile) {
//                Log.d(MainActivity.class.getName(),"disconnect");
//            }
//        }, BluetoothProfile.A2DP);
//
//        if(arduino != null){
//            try {
//                connect.invoke(bt, arduino);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }else{
//            Toast.makeText(MainActivity.this, "Impossible de trouver le module Arduino, assurez-vous d'etre assez pret de lui", Toast.LENGTH_SHORT).show();
//        }


        setDiscoverable();
        if(arduino.getBondState()==arduino.BOND_BONDED) {

            Log.d(MainActivity.class.getName(), arduino.getName());
            //BluetoothSocket mSocket=null;
            try {


                //UUID mUUID = arduino.getUuids()[0].getUuid();


                mSocket = arduino.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                Log.d(MainActivity.class.getName(), "socket not created");
                e1.printStackTrace();
            }
            try {

                mSocket.connect();
                Log.d("Connect","it works");

            } catch (IOException e) {
                try {

                    mSocket.close();
                    Log.d(MainActivity.class.getName(), "Cannot connect");
                    e.printStackTrace();
                } catch (IOException e1) {
                    Log.d(MainActivity.class.getName(), "Socket not closed");
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }


            }
        }else{
            IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
            registerReceiver(new PairingRequest(), filter);
        }

    }

    public void setDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivity(discoverableIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        bluetoothConnect(bluetooth);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK) {
            Log.d(MainActivity.class.getName(),"bluetooth enabled");
            bluetoothConnect(BluetoothAdapter.getDefaultAdapter());
        } else {
            Log.d(MainActivity.class.getName(),"bluetooth always disabled");
            bluetoothConnect(BluetoothAdapter.getDefaultAdapter());
        }
    }

    public void bluetoothConnect(BluetoothAdapter bluetooth){
        if(checkDeviceHasBluetooth(bluetooth)){
            enableBluetooth(bluetooth);
            listDevice = getKnownDevices(bluetooth);
            findArduino(bluetooth);
        }else{
            butchange.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        unregisterReceiver(bluetoothReceiver);
    }

    public static class PairingRequest extends BroadcastReceiver {
        public PairingRequest() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0);
                    //the pin in case you need to accept for an specific pin
                    Log.d("PIN", " " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",0));
                    //maybe you look for a name or address
                    Log.d("Bonded", device.getName());
                    byte[] pinBytes;
                    pinBytes = (""+pin).getBytes("UTF-8");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        device.setPin(pinBytes);
                        //setPairing confirmation if neeeded
                        device.setPairingConfirmation(true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
