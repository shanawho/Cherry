package com.example.shana.cherry.app;

import android.graphics.Color;
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

import com.example.shana.cherry.R;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

public class selectOption extends ActionBarActivity implements BeaconConsumer {

    // Bluetooth Beacon
    private static final String TAG = "[Beacon Scan]";
    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;

    // Hue
    private PHHueSDK phHueSDK;
    private PHBridge bridge;
    private List<PHLight> allLights;

    // Contextual
    String[] choices = new String[]{"3D modeling", "Digital fabrication", "Product design", "Visual design", "Web development"};
    String[] colors = new String[]{"#ef4545", "#f8971c", "#fee101", "#55b847", "#25c4f3"};

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
        if (id == R.id.action_settings) {
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
                    setLightColor(colors[count]);

                }
            });
            radioGroup.addView(radioBtn);

        }
        return true;
    }

    private void setLightColor(String c) {
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            List<Float> colorOut = ColorUtil.colorToXY(c);
            lightState.setX(colorOut.get(0));
            lightState.setY(colorOut.get(1));
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
                if (beacons.size() > 0) {
                    foundBeacon = beacons.iterator().next();
                    final double distance = foundBeacon.getDistance();
                    Log.i(TAG, "The first beacon I see is about " + foundBeacon.getDistance() + " meters away.");
                    // TODO: once working, modify the Hue directly and get it off the UI thread.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parseDistance(distance, true);
                        }
                    });
                } else {
                    Log.i(TAG, "No beacons found in range");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parseDistance(0, false);
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.i(TAG, "Unable to search for beacons.");
        }
    }

    protected void parseDistance(double dis, boolean any) {
        TextView t = (TextView)findViewById(R.id.distance_measurement);
        if (any) {
            String displayedDistance = Double.toString(dis)+" m";
            t.setText("");
        } else {
            t.setText("none found");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

}
