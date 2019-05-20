package com.rarecase.spring;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager packageManager = getPackageManager();
        try {
                    PackageInfo packageInfo = packageManager.getPackageInfo("com.saavn.android",0);
                    if((packageInfo != null) && packageInfo.applicationInfo.enabled) {
                        Log.i("MainActivity:","Found that app! Version: "+packageInfo.versionName);
                        startActivity(new Intent(this, TabActivity.class));
                        MainActivity.this.finish();
                    }else {
                        setContentView(R.layout.activity_main);
                    }
        }catch (PackageManager.NameNotFoundException e) {
            setContentView(R.layout.activity_main);
        }
    }

    public void launchPlaystore(View view) {
        Intent launchIntent= new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.saavn.android"));//.setData(Uri.parse("market://details?id=com.saavn.android"));
        startActivity(launchIntent);
    }
}
