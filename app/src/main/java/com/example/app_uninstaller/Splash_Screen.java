package com.example.app_uninstaller;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        String appVersionCode="";

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            appVersionCode = packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageView splash_icon = findViewById(R.id.splash_icon);
        TextView app_text = findViewById(R.id.splash_text);
        TextView app_version = findViewById(R.id.splash_version);

        Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-Medium.ttf");

        app_version.setText("version "+appVersionCode);
        app_text.setTypeface(typeFace);
        app_version.setTypeface(typeFace);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(splash_icon,"translationY",-600.0f,0.0f,0f);
        objectAnimator.setDuration(2000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(app_text,"translationX",-200.0f,0.0f,0f);
        objectAnimator1.setDuration(2000);
        objectAnimator1.setInterpolator(new LinearInterpolator());
        objectAnimator1.start();

        ObjectAnimator objectAnimator12 = ObjectAnimator.ofFloat(app_version,"translationX",200.0f,0.0f,0f);
        objectAnimator12.setDuration(2000);
        objectAnimator12.setInterpolator(new LinearInterpolator());
        objectAnimator12.start();

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finishAffinity();
            }
        },1000);
    }
}