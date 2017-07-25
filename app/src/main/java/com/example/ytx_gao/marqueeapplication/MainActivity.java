package com.example.ytx_gao.marqueeapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ytx_gao.marqueeview.MarqueeView;
import com.example.ytx_gao.marqueeview.ShimmerFrameLayout;
import com.example.ytx_gao.marqueeview.ShimmerMarqueeLayout;

public class MainActivity extends AppCompatActivity {

    private MarqueeView marqueeView;
    private ShimmerMarqueeLayout container;

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
        marqueeView = (MarqueeView) findViewById(R.id.marquee_view);
        container =
                (ShimmerMarqueeLayout) findViewById(R.id.shimmer_view_container);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marqueeView.startTwinkle();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marqueeView.stopTwinkle();
            }
        });
        findViewById(R.id.shimmerly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (container.isAnimationStarted()) {
                    container.stopShimmerAnimation();
                } else {
                    container.startShimmerAnimation();
                }
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
