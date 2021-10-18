package com.bbotdev.photoeditorlab.Welcome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bbotdev.photoeditorlab.Activity.MainActivity;
import com.bbotdev.photoeditorlab.R;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Menu-Activity. */
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        }, SPLASH_DISPLAY_LENGTH);

    }
}