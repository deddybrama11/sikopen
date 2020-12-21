package com.bram.belajarosmdroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bram.belajarosmdroid.BU.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Preferences.getLoggedInStatus(getApplicationContext())){
                    Intent i = new Intent(SplashActivity.this, MainActivity2.class);
                    startActivity(i);
                    finish();
                }else{
                    finish();
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        },2000);

    }
}