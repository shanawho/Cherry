package com.example.shana.cherry.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.TextView;

import java.util.*;
import com.example.shana.cherry.app.ColorUtil;

//import com.philips.lighting.data.HueSharedPreferences;
import com.example.shana.cherry.R;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import java.io.IOException;

/**
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
**/

public class selectOption extends ActionBarActivity /*implements BeaconConsumer*/ {

    // Bluetooth Beacon
    private static final String DIS_TAG = "[Cherry] [Beacon Distance]";
    private static final String STATE_TAG = "[Cherry] [State]";
    /**
     private RegionBootstrap regionBootstrap;
     private BeaconManager beaconManager;
     */

    // Hue
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private List<PHLight> allLights;

    // Contextual
    String[] choices = new String[]{"3D modeling", "Digital fabrication", "Product design", "Visual design", "Web development"};
    String[] colors = new String[]{"#ef4545", "#f8971c", "#fee101", "#55b847", "#25c4f3"};
    int[] backgrounds = new int[]{R.id.redbg, R.id.orangebg, R.id.yellowbg, R.id.bluebg, R.id.purplebg};
    String userPreference = "#FFFFFF";
    boolean stateOn = false;

    // BLUETOOTH
    private static final UUID MY_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private BluetoothSocket socket = null;
    private BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option);

        // Hue initialization
        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();
        allLights = bridge.getResourceCache().getAllLights();

        /** Beacon scan initialization
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
         **/


        // View initialization
        createRadioGroup();

        adapter = BluetoothAdapter.getDefaultAdapter();

        //FIND OUR UUID AUTOMATICALLY
        connectBluetooth();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_find_bridge) {
            Intent intent = new Intent(getApplicationContext(), PHHomeActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean createRadioGroup() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int j = 0; j < choices.length; j++) {
                    if (checkedId != j) {
                        RadioButton unselectedBtn = (RadioButton) findViewById(j);
                        unselectedBtn.setBackgroundColor(android.R.color.transparent);
                        unselectedBtn.setTextColor(Color.parseColor("#000000"));
                    }
                }
            }
        });

        for (int i = 0; i < choices.length; i++) {
            final RadioButton radioBtn = new RadioButton(this);
            radioBtn.setId(i);
            radioBtn.setText(choices[i]);
            radioBtn.setTextSize(20);
            radioBtn.setId(i);
            radioBtn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            radioBtn.setPadding(50, 75, 0, 75);

            final int count = i;

            radioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioBtn.setBackgroundColor(Color.parseColor(colors[count]));
                    radioBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    setLightPreference(colors[count]);

                    animate(findViewById(backgrounds[count]));
                }
            });
            radioGroup.addView(radioBtn);

        }
        return true;
    }

    // Enable background colors of selected options to change accordingly
    private void animate(View view) {
        /**DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        metrics.heightPixels;
        **/


        //
        for (int bg : backgrounds) {
            View otherView = findViewById(bg);
            if (otherView != view) {
                LayoutParams lp = view.getLayoutParams();
                lp.width = 10;
                otherView.setLayoutParams(lp);
            }
        }

        // animate to full
        ResizeWidthAnimation anim = new ResizeWidthAnimation(view, 100);
        anim.setDuration(1000);
        view.startAnimation(anim);


        /** animate to small
        this.leftFragmentWidthPx = leftFragmentWidthPx;
        LayoutParams lp = (LayoutParams) leftFrame.getLayoutParams();
        lp.width = leftFragmentWidthPx;
        leftFrame.setLayoutParams(lp);
        **/

    }
    private void setLightPreference(String c) {
        this.userPreference = c;
        if (stateOn) {
            setLightColor(c);
        }
    }

    private void setLightColor(String c) {
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setOn(true);
            List<Float> colorOut = ColorUtil.colorToXY(c);
            lightState.setX(colorOut.get(0));
            lightState.setY(colorOut.get(1));
            bridge.updateLightState(light, lightState);
        }
    }

    private void turnLightOff() {
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setOn(false);
        }
    }

    /** Beacon code
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // TODO: 'region' should be defined as the active space around the chair to light.
                Beacon foundBeacon;
                if (beacons.size() > 0) {
                    foundBeacon = beacons.iterator().next();
                    final double distance = foundBeacon.getDistance();
                    Log.i(DIS_TAG, "found " + foundBeacon.getDistance() + " meters away.");
                    parseDistance(distance, true);
                } else {
                    Log.i(DIS_TAG, "None.");
                    parseDistance(0, false);
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.i(DIS_TAG, "Unable to search for beacons.");
        }
    }

    protected void parseDistance(double dis, boolean any) {
        if (any && (dis <= 0.4)) {
            if (!stateOn) {
                stateOn = true;
                Log.d(STATE_TAG,"ON, color="+this.userPreference);
                setLightColor(this.userPreference);
            }
        } else {
            if (stateOn) {
                stateOn = false;
                Log.d(STATE_TAG, "OFF.");
                setLightColor("#FFFFFF");
                //turnLightOff();
            }
        }
    }
     **/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //beaconManager.unbind(this);
    }

    private BluetoothSocket connectBluetooth(BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            return socket;
        } catch (IOException e) {
            close(socket);
        } catch (Exception ignore) {
            return null;
        }
    }


    // Main BTLE device callback where much of the logic occurs.
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(DIS_TAG, "Connected!");
                updateConnectionStatus("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    Log.i(DIS_TAG, "Failed to start discovering services!");
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(DIS_TAG, "Disconnected!");
                updateConnectionStatus("Disconnected!");
            } else {
                Log.i(DIS_TAG, "Connection state changed.  New state: " + newState);
                updateConnectionStatus("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(DIS_TAG, "Service discovery completed!");
            } else {
                Log.i(DIS_TAG, "Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            } else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }


        // BTLE device scanning callback.
        private LeScanCallback scanCallback = new LeScanCallback() {
            // Called when a device is found.
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                Log.i(DIS_TAG, "Found device: " + bluetoothDevice.getAddress() + " with name " + bluetoothDevice.getName());
                // Check if the device has the UART service and is called pgao
                if (parseUUIDs(bytes).contains(MY_UUID) && bluetoothDevice.getName().equals("Cherry")) {
                    // Found a device, stop the scan.
                    adapter.stopLeScan(scanCallback);
                    Log.i(DIS_TAG, "Found UART service!");
                    // Connect to the device.
                    // Control flow will now go to the callback functions when BTLE events occur.
                    gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                }
            }
        };
    };

}
