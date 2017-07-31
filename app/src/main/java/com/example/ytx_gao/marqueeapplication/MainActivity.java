package com.example.ytx_gao.marqueeapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ytx_gao.marqueeapplication.widget.HJJTZXIndicatorView;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    private int[] targetAlphas;

    private HJJTZXIndicatorView hjjtzxIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {
        targetAlphas = new int[16];
        for (int j = 0; j < 16; j++) {
            targetAlphas[j] = (int) (255 * (1 - (float) j / 16));
        }
        hjjtzxIndicatorView = (HJJTZXIndicatorView) findViewById(R.id.hjjtzx_indicator);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, MeterViewTestActivity.class));
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.shimmerly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hjjtzxIndicatorView.setHJJTData(targetAlphas);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
