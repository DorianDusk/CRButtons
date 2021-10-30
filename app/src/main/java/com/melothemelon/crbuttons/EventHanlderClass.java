package com.melothemelon.crbuttons;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventHanlderClass {
    private static final String LOG_TAG = "EVENTHANDLER";
    private static MediaPlayer mediaPlayer;
    private static DatabaseHandler databaseHandler;

    public static void startMediaPlayer(View view, Integer soundID){
        try{
            if(soundID != null){
                if(mediaPlayer != null)
                    mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(view.getContext(), soundID);
                mediaPlayer.start();
            }
        }catch(Exception e){
            Log.e(LOG_TAG, "Failed to initialize Media Player: " + e.getMessage());
        }
    }

    public static void releaseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void popupManager(final View view, final SoundObject soundObject){
        databaseHandler = new DatabaseHandler(view.getContext());
        PopupMenu popup = new PopupMenu(view.getContext(), view);

        if(view.getContext() instanceof FavoriteActivity)
            popup.getMenuInflater().inflate(R.menu.favorites_longclick, popup.getMenu());
        else
            popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_favorite){
                 if(view.getContext() instanceof FavoriteActivity)
                     databaseHandler.removeFavorite(view.getContext(), soundObject, true);
                 else
                     databaseHandler.addFavorite(soundObject);
                }
                return true;
            }
        });
        popup.show();
    }

}
