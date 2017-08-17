package com.example.android.ltcteminer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Tal on 07/08/2017.
 */

public class SplashScreen extends Activity {

    // Splash screen timer
   // private static int SPLASH_TIME_OUT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);
        finish();
      //  setContentView(R.layout.splash_screen);

//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//
//                Intent i = new Intent(SplashScreen.this, MainActivity.class);
//                startActivity(i);
//                finish();
//            }
//        }, SPLASH_TIME_OUT);
    }

}
