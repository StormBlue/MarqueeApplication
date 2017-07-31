package com.example.ytx_gao.marqueeapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.ytx_gao.marqueeapplication.widget.HJJTZXIndicatorView;
import com.example.ytx_gao.marqueeview.HJJTZXMeterView;

public class MeterViewTestActivity extends AppCompatActivity {

    private HJJTZXMeterView hjjtzxIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_view_test);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        hjjtzxIndicatorView = (HJJTZXMeterView)findViewById(R.id.hjjtzx_indicator);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                hjjtzxIndicatorView.startRotateAnimation();
            }
        });
    }
}
