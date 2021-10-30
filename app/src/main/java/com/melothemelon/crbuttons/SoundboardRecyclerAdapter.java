package com.melothemelon.crbuttons;

import android.app.Activity;
import android.media.Image;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;

public class SoundboardRecyclerAdapter extends RecyclerView.Adapter<SoundboardRecyclerAdapter.SoundboardViewHolder> {

    private ArrayList<SoundObject> soundObjects;
    private static String LOG_TAG = "[SoundboardRecyclerAdapter]";

    private InterstitialAd mInterstitialAd;


    public SoundboardRecyclerAdapter(ArrayList<SoundObject> soundObjects){
        this.soundObjects = soundObjects;
    }

    @NonNull
    @Override
    public SoundboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sounditem, null);
        return new SoundboardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundboardViewHolder holder, int position) {
        final SoundObject object = soundObjects.get(position);
        final Integer soundID = object.getItemID();
        final String characterName = object.getCharacterName();
        holder.itemTextView.setText(object.getItemName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventHanlderClass.startMediaPlayer(v, soundID);
            }
        });
        ImageView img = (ImageView) holder.itemView.findViewById(R.id.imageViewItem);
        int id = img.getContext().getResources().getIdentifier(characterName.toLowerCase(), "drawable", img.getContext().getPackageName());
        img.setImageDrawable(img.getContext().getResources().getDrawable(id));
        DatabaseHandler databaseHandler = new DatabaseHandler(holder.itemTextView.getContext());
        if(databaseHandler.isInFavorites(object)){
            ImageButton imgButton = (ImageButton) holder.itemView.findViewById(R.id.favorButton);
            int starId = imgButton.getContext().getResources().getIdentifier("starfull", "drawable", imgButton.getContext().getPackageName());
            imgButton.setImageDrawable(imgButton.getContext().getResources().getDrawable(starId));
        }
        /* Not needed due to new favorite button
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EventHanlderClass.popupManager(v, object);
                return true;
            }
        });
         */
    }

    @Override
    public int getItemCount() {
        return soundObjects.size();
    }

    public class SoundboardViewHolder extends RecyclerView.ViewHolder{

        TextView itemTextView;

        public SoundboardViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = (TextView) itemView.findViewById(R.id.textViewItem);
        }
    }
}
