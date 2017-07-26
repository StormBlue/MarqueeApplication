package com.example.ytx_gao.marqueeapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ytx_gao.marqueeview.ShimmerMarqueeView;

public class MainActivity extends AppCompatActivity {

    private ShimmerMarqueeView marqueeView;

    private int[] targetColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {
        targetColors = new int[16];
        for (int j = 0; j < 16; j++) {
            targetColors[j] = Color.argb((int) (255 * (1 - (float) j / 16)), 0, 245, 170);
        }
        marqueeView =
                (ShimmerMarqueeView) findViewById(R.id.shimmer_view);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marqueeView.setLightTargetColors(targetColors);
                marqueeView.startAnimations();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marqueeView.stopAnimations();
            }
        });
        findViewById(R.id.shimmerly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
