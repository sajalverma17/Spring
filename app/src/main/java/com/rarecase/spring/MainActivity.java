package com.rarecase.spring;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PackageManager packageManager = getPackageManager();
            List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo packageInfo:packageInfoList) {
                //App's package name has changed now, this is to support the new as well as the old app
                //Anyone of them will do, if none of them are found, route to GooglePlay asking to download new package name
                if(Objects.equals(packageInfo.packageName, "com.jio.media.jiobeats") || Objects.equals(packageInfo.packageName, "com.saavn.android")){
                    if(packageInfo.applicationInfo.enabled) {
                        Log.i("MainActivity:","Found that app! Version: "+packageInfo.versionName);
                        startActivity(new Intent(this, TabActivity.class));
                        MainActivity.this.finish();
                    }else {
                        setContentView(R.layout.activity_main);
                    }
                }
                else{
                    setContentView(R.layout.activity_main);
                }
            }
    }

    public void launchPlaystore(View view) {
        Intent launchIntent= new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.jio.media.jiobeats"));//.setData(Uri.parse("market://details?id=com.saavn.android"));
        startActivity(launchIntent);
    }
}
