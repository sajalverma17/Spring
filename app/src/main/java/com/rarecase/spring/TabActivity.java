package com.rarecase.spring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.rarecase.utils.SpringSharedPref;

public class TabActivity extends AppCompatActivity {

    static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;
    TabViewPagerAdapter tabViewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ViewPager mPager = (ViewPager) findViewById(R.id.tabViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        Fragment offLineSongsFragment = new HomeActivity();
        Fragment lastSharedSongsFragment = new SharedSongListActivity();

        tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager());
        tabViewPagerAdapter.addFragment(offLineSongsFragment,"Offline");
        tabViewPagerAdapter.addFragment(lastSharedSongsFragment,"Recently Shared");
        mPager.setAdapter(tabViewPagerAdapter);

        tabLayout.setupWithViewPager(mPager);
        requestStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults){
        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Current Fragment's presenter will be called here. Get current from fragment
                ISongListView currentFragment = (ISongListView) tabViewPagerAdapter.getItem(0);
                currentFragment.getPresenter().loadOfflineSongs();
            }else{
                Toast.makeText(this,this.getString(R.string.please_grant_storage_permission),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_item_run_demo_again:

                SpringSharedPref pref = new SpringSharedPref(this);
                pref.setFirstTime(true);
                startActivity(new Intent(this, WelcomeActivity.class));

                break;
        }
        return true;
    }

    public void requestStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }
}


