package com.zappkit.zappid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import com.zappkit.zappid.lemeor.MainMenuActivity;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_DISPLAY_LENGTH = 1500;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                boolean islogin = SharedPreferenceHelper.getInstance(SplashActivity.this).getBool("islogin");
                Intent mainIntent;
                if(islogin)
                {
                    mainIntent = new Intent(SplashActivity.this, MainMenuActivity.class);
                }
                else
                {
                    mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}