package com.example.frank.flowshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.frank.flowshare.activity.FlowActivity;
import com.example.frank.flowshare.activity.WifiActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent=new Intent(HomeActivity.this, WifiActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
