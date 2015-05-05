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
import android.graphics.Typeface;
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


import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.distance.DistanceCalculator;


public class selectOption extends ActionBarActivity implements BeaconConsumer {

    // Bluetooth Beacon
    private static final String DIS_TAG = "[Cherry] [Beacon Distance]";
    private static final String STATE_TAG = "[Cherry] [State]";

    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;
    private DistanceCalculator calc;


    // Hue
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private List<PHLight> allLights;

    // Contextual
    String[] choices = new String[]{"3D modeling", "Digital fabrication", "Product design", "Visual design", "Web development"};
    int[] colors = new int[]{R.color.red, R.color.orange, R.color.yellow, R.color.blue, R.color.purple};
    int[] backgrounds = new int[]{R.id.redbg, R.id.orangebg, R.id.yellowbg, R.id.bluebg, R.id.purplebg};
    String userPreference = "#FFFFFF";
    boolean stateOn = false;

    // BLUETOOTH


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option);

        // Hue initialization

        phHueSDK = PHHueSDK.create();
        bridge = phHueSDK.getSelectedBridge();
        allLights = bridge.getResourceCache().getAllLights();


        // Beacon scan initialization
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);



        // View initialization
        createRadioGroup();

        // BLUETOOTH
        //UUID MY_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

        //FIND OUR UUID AUTOMATICALLY

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_option, menu);
        findViewById(R.menu.menu_select_option).setTypeface
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
            radioBtn.setPadding(0, 79, 0, 79);

            final int count = i;

            radioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioBtn.setTextColor(Color.parseColor("#FFFFFF"));

                    setLightPreference(String.valueOf(getResources().getString(colors[count])));
                    animate(findViewById(backgrounds[count]));
                }
            });
            radioGroup.addView(radioBtn);

        }
        return true;
    }

    // Enable background colors of selected options to change accordingly
    private void animate(View view) {

        for (int bg : backgrounds) {
            View otherView = findViewById(bg);
            if (otherView != view && R.dimen.margin_left != otherView.getWidth()) {
                ResizeWidthAnimation animSmall = new ResizeWidthAnimation(otherView, (int) getResources().getDimension(R.dimen.margin_left));
                animSmall.setDuration(300);
                otherView.startAnimation(animSmall);
            }
        }
        // animate to full
        ResizeWidthAnimation anim = new ResizeWidthAnimation(view, 1200);
        anim.setDuration(300);
        view.startAnimation(anim);


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
            bridge.updateLightState(light, lightState);

        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // TODO: 'region' should be defined as the active space around the chair to light.
                Beacon foundBeacon;
                int rssi = 0;
                int tx = 0;
                double dis = 0.0;
                if (beacons.size() > 0) {
                    foundBeacon = beacons.iterator().next();
                    calc = foundBeacon.getDistanceCalculator();
                    rssi = foundBeacon.getRssi();
                    tx = foundBeacon.getTxPower();
                    dis = calc.calculateDistance(rssi,tx);
                    Log.i(DIS_TAG, "found with distance: "+Double.toString(dis));
                    parseDistance(dis, true);
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
        if (any && (dis > 100)) {
            if (!stateOn) {
                stateOn = true;
                Log.d(STATE_TAG,"ON, color="+this.userPreference);
                setLightColor(this.userPreference);
            }
        } else {
            if (stateOn) {
                stateOn = false;
                Log.d(STATE_TAG, "OFF.");
                turnLightOff();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

}
