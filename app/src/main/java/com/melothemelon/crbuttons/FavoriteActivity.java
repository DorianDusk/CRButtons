package com.melothemelon.crbuttons;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "FavoriteActivity";
    Toolbar toolbar;
    ArrayList<SoundObject> favoriteList = new ArrayList<>();
    RecyclerView favoriteView;
    SoundboardRecyclerAdapter favoriteAdapter = new SoundboardRecyclerAdapter(favoriteList);
    RecyclerView.LayoutManager favoriteLayoutManager;
    private View mLayout;
    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        databaseHandler = new DatabaseHandler(this);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.soundboard_toolbar);
        setSupportActionBar(toolbar);
        //Load Ads
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        addDataToArrayList();
        favoriteView = (RecyclerView) findViewById(R.id.soundboardRecyclerView);
        favoriteLayoutManager = new GridLayoutManager(this, 3);
        favoriteView.setLayoutManager(favoriteLayoutManager);
        favoriteView.setAdapter(favoriteAdapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventHanlderClass.releaseMediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_favorites, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_favorite_hide)
            this.startActivity(new Intent(this, MainActivity.class));
            //finish();
        return super.onOptionsItemSelected(item);
    }

    private void addDataToArrayList(){
        favoriteList.clear();
        Cursor cursor = databaseHandler.getFavorites();
        if(cursor.getCount() == 0){
            Log.e(LOG_TAG, "Cursor Empty or Data couldn't be loaded.");
            cursor.close();
        } else {
            if(cursor.getCount() != favoriteList.size()){
                while(cursor.moveToNext()){
                    String NAME = cursor.getString(cursor.getColumnIndex(DatabaseSchema.FavoritesTable.NAME));
                    String CHARACTERNAME = cursor.getString(cursor.getColumnIndex(DatabaseSchema.FavoritesTable.CHARACTERNAME));
                    Integer ID = cursor.getInt(cursor.getColumnIndex(DatabaseSchema.FavoritesTable.RESOURCE_ID));
                    favoriteList.add(new SoundObject(NAME, CHARACTERNAME, ID));
                    favoriteAdapter.notifyDataSetChanged();
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
            databaseHandler.removeFavorite(v.getContext(), soundObject, true);
        }else{
            int starId = imgButton.getContext().getResources().getIdentifier("starfull", "drawable", imgButton.getContext().getPackageName());
            imgButton.setImageDrawable(imgButton.getContext().getResources().getDrawable(starId));
            databaseHandler.addFavorite(soundObject);
        }
    }


}
