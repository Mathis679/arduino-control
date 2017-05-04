package com.example.iem.arduinocontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView tv_noconnect;
    Button butchange;
    LinearLayout bg;
    boolean basemode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_noconnect = (TextView) findViewById(R.id.tv_noconnect);
        butchange = (Button) findViewById(R.id.butchangemod);
        bg = (LinearLayout) findViewById(R.id.ll_bg);
        basemode = false;
        butchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                basemode = true;
                //bg.setBackgroundColor(R.color.colorPrimary);
            }
        });
    }
}
