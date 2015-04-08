package com.example.shana.cherry;

import android.app.ActionBar;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

//import com.philips.lighting.data.AccessPointListAdapter;
//import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;

public class selectOption extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option);

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

    String[] choices = new String[]{"3D modeling", "Digital fabrication", "Product design", "Visual design", "Web development"};
    String[] colors = new String[]{"#ef4545", "#f8971c", "#fee101", "#55b847", "#25c4f3"};

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

                }
            });

            radioGroup.addView(radioBtn);

        }
        return true;
    }

}
