package com.melothemelon.crbuttons;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.AppCompatTextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Toolbar toolbar;

    ArrayList<SoundObject> soundList = new ArrayList<>();

    RecyclerView soundView;
    SoundboardRecyclerAdapter soundAdapter = new SoundboardRecyclerAdapter(soundList);
    RecyclerView.LayoutManager soundLayoutManager;

    private View mLayout;

    DatabaseHandler databaseHandler = new DatabaseHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this);

        //databaseHandler.appUpdate();
        //databaseHandler.createSoundCollection(this);
        //databaseHandler.dropFavorites();
        //databaseHandler.updateFavorites();

        if(appUpdate()) {
            Log.d(LOG_TAG, "App Update");
            databaseHandler.createSoundCollection(this);
            databaseHandler.updateFavorites();
        }
        //databaseHandler.createSoundCollection(this);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.activity_soundboard);

        toolbar = (Toolbar) findViewById(R.id.soundboard_toolbar);
        setSupportActionBar(toolbar);

        //Load Ads
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Rate us Dialog
        AppRate.with(this)
                .setInstallDays(0)
                .setLaunchTimes(3)
                .setRemindInterval(1)
                .setShowLaterButton(true)
                .setDebug(false)
                .setOnClickButtonListener(new OnClickButtonListener() {
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        addDataToArrayList();

        soundView = (RecyclerView) findViewById(R.id.soundboardRecyclerView);
        soundLayoutManager = new GridLayoutManager(this, 3); //3 is the amount of buttons per row
        soundView.setLayoutManager(soundLayoutManager);
        soundView.setAdapter(soundAdapter);

        //requestPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventHanlderClass.releaseMediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_favorite_show)
            this.startActivity(new Intent(this, FavoriteActivity.class));
        return super.onOptionsItemSelected(item);
    }

    private void requestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            if(!Settings.System.canWrite(this)){
                Snackbar.make(mLayout, "Critical Role Buttons needs access to your settings, to set sounds as ringtone", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
            }
        }
    }

    //Always have this in the first activity the App starts in
    private boolean appUpdate(){
        final String PREFS_NAME = "VersionPref";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        int currentVersionCode = 0;
        try{
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        }catch (PackageManager.NameNotFoundException e){
            Log.e(LOG_TAG, e.getMessage());
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        SharedPreferences.Editor edit = prefs.edit();

        if(savedVersionCode == DOESNT_EXIST || currentVersionCode > savedVersionCode){
            databaseHandler.appUpdate();
            edit.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            edit.commit();
            return true;
        }

        return false;
    }

    private void addDataToArrayList(){
        soundList.clear();
        Cursor cursor = databaseHandler.getSoundCollection();
        if(cursor.getCount() == 0){
            Log.e(LOG_TAG, "Cursor Empty or Data couldn't be loaded.");
            cursor.close();
        } else {
            if(cursor.getCount() != soundList.size()){
                while(cursor.moveToNext()){
                    String NAME = cursor.getString(cursor.getColumnIndex(DatabaseSchema.MainTable.NAME));
                    String CHARACTERNAME = cursor.getString(cursor.getColumnIndex(DatabaseSchema.MainTable.CHARACTERNAME));
                    Integer ID = cursor.getInt(cursor.getColumnIndex(DatabaseSchema.MainTable.RESOURCE_ID));
                    soundList.add(new SoundObject(NAME, CHARACTERNAME, ID));
                    soundAdapter.notifyDataSetChanged();
                }
            }
            cursor.close();
        }
    }

    public void favorButtonOnClick(View v){
        ImageButton imgButton = (ImageButton) v;
        RelativeLayout rl = (RelativeLayout) imgButton.getParent();
        AppCompatTextView text = (AppCompatTextView) rl.getChildAt(2);
        String soundName = text.getText().toString();
        SoundObject soundObject = databaseHandler.getSoundObjectFromName(soundName);
        if(databaseHandler.isInFavorites(soundObject)){
            int starId = imgButton.getContext().getResources().getIdentifier("starempty", "drawable", imgButton.getContext().getPackageName());
            imgButton.setImageDrawable(imgButton.getContext().getResources().getDrawable(starId));
            databaseHandler.removeFavorite(v.getContext(), soundObject, false);
        }else{
            int starId = imgButton.getContext().getResources().getIdentifier("starfull", "drawable", imgButton.getContext().getPackageName());
            imgButton.setImageDrawable(imgButton.getContext().getResources().getDrawable(starId));
            databaseHandler.addFavorite(soundObject);
        }
    }
}