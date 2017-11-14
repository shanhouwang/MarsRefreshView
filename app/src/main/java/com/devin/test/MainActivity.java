package com.devin.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.devin.refreshview.MarsRefreshView;

public class MainActivity extends AppCompatActivity {

    private MarsRefreshView mMarsRefreshView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMarsRefreshView = findViewById(R.id.marsRefreshView);
    }
}
