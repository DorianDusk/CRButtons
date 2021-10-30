package com.melothemelon.crbuttons;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import com.melothemelon.crbuttons.DatabaseSchema.MainTable;
import com.melothemelon.crbuttons.DatabaseSchema.FavoritesTable;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DATABASE_HANDLER";
    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 1;

    //table creation statements
    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS " + MainTable.TABLE_NAME + "(" +
            MainTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MainTable.NAME + " TEXT, " +
            MainTable.CHARACTERNAME + " TEXT, " +
            MainTable.RESOURCE_ID + " INTEGER unique);";
    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + FavoritesTable.TABLE_NAME + "(" +
            FavoritesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FavoritesTable.NAME + " TEXT, " +
            FavoritesTable.CHARACTERNAME + " TEXT, " +
            FavoritesTable.RESOURCE_ID + " INTEGER);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(SQL_CREATE_MAIN_TABLE);
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }catch (Exception e){
            Log.e(LOG_TAG, "Failed to initialize database: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
        onCreate(db);
    }

    public void resetFavorites(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + FavoritesTable.TABLE_NAME);
        database.execSQL(SQL_CREATE_FAVORITES_TABLE);
        database.close();
    }

    public void createSoundCollection(Context context){
        List<String> nameList = Arrays.asList(context.getResources().getStringArray(R.array.soundNames));
        SoundObject[] soundItems = getSoundItemArray(nameList);
        for (SoundObject i: soundItems)
            putIntoMain(i);
    }

    private void putIntoMain(SoundObject soundObject){
        SQLiteDatabase database = this.getWritableDatabase();
        if(!verification(database, MainTable.TABLE_NAME, MainTable.RESOURCE_ID, soundObject.getItemID())){
            try{
                ContentValues contentValues = new ContentValues();
                contentValues.put(MainTable.NAME, soundObject.getItemName());
                contentValues.put(MainTable.CHARACTERNAME, soundObject.getCharacterName());
                contentValues.put(MainTable.RESOURCE_ID, soundObject.getItemID());
                database.insert(MainTable.TABLE_NAME, null, contentValues);
            }catch (Exception e){
                Log.e(LOG_TAG, "(MAIN) Error when loading sound: " + e.getMessage());
            } finally {
                database.close();
            }
        }
    }

    public boolean isInFavorites(SoundObject soundObject){
        boolean isInFavo = false;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + FavoritesTable.TABLE_NAME + " WHERE " + FavoritesTable.RESOURCE_ID + " = " + soundObject.getItemID(), null);
        cursor.moveToFirst();
        if(cursor.getInt(0) > 0)
            isInFavo = true;
        cursor.close();
        database.close();
        return isInFavo;
    }

    private boolean verification(SQLiteDatabase database, String tableName, String idColumn, Integer soundId){
        int count = -1;
        Cursor cursor = null;
        try{
            String query = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = " + soundId;
            cursor = database.rawQuery(query, null);
            if(cursor.moveToFirst())
                count = cursor.getInt(0);
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return (count > 0);
    }

    public Cursor getSoundCollection(){
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + MainTable.TABLE_NAME + " ORDER BY " + MainTable.CHARACTERNAME + ", " + MainTable.NAME, null);
    }

    public Cursor getFavorites(){
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + FavoritesTable.TABLE_NAME + " ORDER BY " + FavoritesTable.CHARACTERNAME + ", " + FavoritesTable.NAME, null);
    }

    public void addFavorite(SoundObject soundObject){
        SQLiteDatabase database = this.getWritableDatabase();
        if(!verification(database, FavoritesTable.TABLE_NAME, FavoritesTable.RESOURCE_ID, soundObject.getItemID())){
            try{
                ContentValues contentValues = new ContentValues();
                contentValues.put(FavoritesTable.NAME, soundObject.getItemName());
                contentValues.put(FavoritesTable.CHARACTERNAME, soundObject.getCharacterName());
                contentValues.put(FavoritesTable.RESOURCE_ID, soundObject.getItemID());
                database.insert(FavoritesTable.TABLE_NAME, null, contentValues);
            }catch (Exception e){
                Log.e(LOG_TAG, "(FAVORITES) Error when loading sound: " + e.getMessage());
            } finally {
                database.close();
            }
        }
    }

    public void removeFavorite(Context context, SoundObject soundObject, boolean refresh){
        SQLiteDatabase database = this.getWritableDatabase();
        if(verification(database, FavoritesTable.TABLE_NAME, FavoritesTable.RESOURCE_ID, soundObject.getItemID())){
            try{
                database.delete(FavoritesTable.TABLE_NAME, FavoritesTable.NAME + " = '" + soundObject.getItemName() + "'", null);
                if(refresh) {
                    Activity activity = (Activity) context;
                    Intent intent = activity.getIntent();
                    activity.overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    context.startActivity(intent);
                }
            }catch (Exception e){
                Log.e(LOG_TAG, "(FAVORITES) Error when removing a favorite sound: " + e.getMessage());
            } finally {
                database.close();
            }
        }
    }

    public SoundObject getSoundObjectFromName(String name){
        String characterName = "";
        int id = -1;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + MainTable.TABLE_NAME + " WHERE " + MainTable.NAME + " = '" + name + "'", null);
        cursor.moveToFirst();
        characterName = cursor.getString(2);
        id = cursor.getInt(3);
        Log.e(LOG_TAG, "CharachterName: " + characterName);


        cursor.close();
        database.close();
        return new SoundObject(name, characterName, id);
    }

    public void updateFavorites(){
        SQLiteDatabase database = this.getWritableDatabase();
        try{
            Cursor favorite_content = database.rawQuery("SELECT * FROM " + FavoritesTable.TABLE_NAME, null);
            if(favorite_content.getCount() > 0){
                while(favorite_content.moveToNext()){
                    String entryName = favorite_content.getString(favorite_content.getColumnIndex(FavoritesTable.TABLE_NAME));
                    Cursor updateEntry = database.rawQuery("SELECT * FROM " + MainTable.TABLE_NAME + " WHERE " + MainTable.NAME + " = '" + entryName + "'", null);
                    if(updateEntry.getCount() > 0 ){
                        updateEntry.moveToFirst();
                        if(favorite_content.getInt(favorite_content.getColumnIndex(FavoritesTable.RESOURCE_ID)) != updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID))){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(FavoritesTable.RESOURCE_ID, updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID)));
                            database.update(FavoritesTable.TABLE_NAME, contentValues, FavoritesTable.NAME + " = '" + entryName + "'", null);
                        }
                    }
                    updateEntry.close();
                }
            }
            favorite_content.close();
        } catch (Exception e){
            Log.e(LOG_TAG, "(FAVORITES) Failed to update favorites: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    public void appUpdate(){
        SQLiteDatabase database = this.getWritableDatabase();
        try{
            database.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
            database.execSQL(SQL_CREATE_MAIN_TABLE);
        }catch (Exception e){
            Log.e(LOG_TAG, "Error while updating the main table: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    public void dropFavorites(){
        SQLiteDatabase database = this.getWritableDatabase();
        try{
            database.execSQL("DROP TABLE IF EXISTS " + FavoritesTable.TABLE_NAME);
            database.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }catch (Exception e){
            Log.e(LOG_TAG, "Error while updating the favorite table: " + e.getMessage());
        } finally {
            database.close();
        }
    }

    private SoundObject[] getSoundItemArray(List<String> nameList) {
        SoundObject[] soundItems = {new SoundObject(nameList.get(0), "Grog", R.raw.beer),
                new SoundObject(nameList.get(1),"Grog", R.raw.doigettohitthings),
                new SoundObject(nameList.get(2),"Grog", R.raw.fixhim),
                new SoundObject(nameList.get(3),"Grog", R.raw.grogstrongjaw),
                new SoundObject(nameList.get(4),"Grog", R.raw.iapproveofthisplan),
                new SoundObject(nameList.get(5),"Grog", R.raw.intelligenceof6),
                new SoundObject(nameList.get(6),"Grog", R.raw.likeaflowergirl),
                new SoundObject(nameList.get(7),"Grog", R.raw.liketorage),
                new SoundObject(nameList.get(8),"Grog", R.raw.nice),
                new SoundObject(nameList.get(9),"Grog", R.raw.nobodydiesaroundvoxmachina),
                new SoundObject(nameList.get(10),"Grog", R.raw.profgrog),
                new SoundObject(nameList.get(11),"Grog", R.raw.themeatoutofyourdome),
                new SoundObject(nameList.get(12),"Grog", R.raw.touchmyaxe),
                new SoundObject(nameList.get(13), "Keyleth", R.raw.icanrpthis),
                new SoundObject(nameList.get(14), "Keyleth", R.raw.ohhallo),
                new SoundObject(nameList.get(15), "Keyleth", R.raw.turnintoagoldfish),
                new SoundObject(nameList.get(16), "Keyleth", R.raw.underwaterteaparty),
                new SoundObject(nameList.get(17), "Keyleth", R.raw.waitwhat),
                new SoundObject(nameList.get(18), "Keyleth", R.raw.wearegods),
                new SoundObject(nameList.get(19), "Scanlan", R.raw.burtreynoldsesquire),
                new SoundObject(nameList.get(20), "Scanlan", R.raw.clarotahealing),
                new SoundObject(nameList.get(21), "Scanlan", R.raw.getyourscanlanon),
                new SoundObject(nameList.get(22), "Scanlan", R.raw.iamacrimelord),
                new SoundObject(nameList.get(23), "Scanlan", R.raw.ihealedyousobad),
                new SoundObject(nameList.get(24), "Scanlan", R.raw.imabeliever),
                new SoundObject(nameList.get(25), "Scanlan", R.raw.iwalkby),
                new SoundObject(nameList.get(26), "Scanlan", R.raw.scanlanmakeyoufeelgood),
                new SoundObject(nameList.get(27), "Scanlan", R.raw.shittingonabed),
                new SoundObject(nameList.get(28), "Scanlan", R.raw.youareugly),
                new SoundObject(nameList.get(29), "Scanlan", R.raw.youarewalkingstraight),
                new SoundObject(nameList.get(30), "Scanlan", R.raw.yougonnafailthatattack),
                new SoundObject(nameList.get(31), "Pike", R.raw.allkindsoffuckedup),
                new SoundObject(nameList.get(32), "Pike", R.raw.guidingbolt),
                new SoundObject(nameList.get(33), "Pike", R.raw.hairyshoulders),
                new SoundObject(nameList.get(34), "Pike", R.raw.itsgoingtobefine),
                new SoundObject(nameList.get(35), "Pike", R.raw.killyouall),
                new SoundObject(nameList.get(36), "Pike", R.raw.okayokayokay),
                new SoundObject(nameList.get(37), "Pike", R.raw.turnthatfrownupsidedown),
                new SoundObject(nameList.get(38), "Pike", R.raw.vasinvagina),
                new SoundObject(nameList.get(39), "Pike", R.raw.whatafailofaturn),
                new SoundObject(nameList.get(40), "Matt", R.raw.glorious),
                new SoundObject(nameList.get(41), "Matt", R.raw.hdywtdt),
                new SoundObject(nameList.get(42), "Matt", R.raw.huhsure),
                new SoundObject(nameList.get(43), "Matt", R.raw.whoisnext),
                new SoundObject(nameList.get(44), "Matt", R.raw.madammusk),
                new SoundObject(nameList.get(45), "Matt", R.raw.youareinmydomain),
                new SoundObject(nameList.get(46), "Matt", R.raw.iamkiri),
                new SoundObject(nameList.get(47), "Matt", R.raw.gofuckyourself),
                new SoundObject(nameList.get(48), "Percy", R.raw.iamsorryiamagenius),
                new SoundObject(nameList.get(49), "Percy", R.raw.iamsureyouhaveafriend),
                new SoundObject(nameList.get(50), "Percy", R.raw.idontspeakfish),
                new SoundObject(nameList.get(51), "Percy", R.raw.iwantmymoneyback),
                new SoundObject(nameList.get(52), "Percy", R.raw.lifeneedsthingstolive),
                new SoundObject(nameList.get(53), "Percy", R.raw.likea14yearold),
                new SoundObject(nameList.get(54), "Percy", R.raw.percival),
                new SoundObject(nameList.get(55), "Percy", R.raw.wekeepyourweapons),
                new SoundObject(nameList.get(56), "Percy", R.raw.youhaveadeal),
                new SoundObject(nameList.get(57), "Percy", R.raw.yoursecretissafe),
                new SoundObject(nameList.get(58), "Vax", R.raw.comebackwhenyouareready),
                new SoundObject(nameList.get(59), "Vax", R.raw.daggerdaggerdagger),
                new SoundObject(nameList.get(60), "Vax", R.raw.goodlooking),
                new SoundObject(nameList.get(61), "Vax", R.raw.iamofferingyouanexperience),
                new SoundObject(nameList.get(62), "Vax", R.raw.inthedarkness),
                new SoundObject(nameList.get(63), "Vax", R.raw.phukungt),
                new SoundObject(nameList.get(64), "Vax", R.raw.prayertosarenrae),
                new SoundObject(nameList.get(65), "Vax", R.raw.taconothotdog),
                new SoundObject(nameList.get(66), "Vax", R.raw.vaxinateyourkids),
                new SoundObject(nameList.get(67), "Vex", R.raw.averydeepquestion),
                new SoundObject(nameList.get(68), "Vex", R.raw.dirtyoldbooks),
                new SoundObject(nameList.get(69), "Vex", R.raw.doyouunderstandme),
                new SoundObject(nameList.get(70), "Vex", R.raw.iknow),
                new SoundObject(nameList.get(71), "Vex", R.raw.limerick),
                new SoundObject(nameList.get(72), "Vex", R.raw.prettyshitty),
                new SoundObject(nameList.get(73), "Vex", R.raw.secretsarentsave),
                new SoundObject(nameList.get(74), "Vex", R.raw.stillbeugly),
                new SoundObject(nameList.get(75), "Vex", R.raw.stillpretendingtolikethisfuck),
                new SoundObject(nameList.get(76), "Vex", R.raw.trinketthewonderbear),
                new SoundObject(nameList.get(77), "Vex", R.raw.whohasmimosa),
                new SoundObject(nameList.get(78), "Vex", R.raw.whyareyousoevil),
                new SoundObject(nameList.get(79), "Vex", R.raw.youareadipshit),
                new SoundObject(nameList.get(80), "Vex", R.raw.yourbroomfellinthelava),
                new SoundObject(nameList.get(81), "Matt", R.raw.bye),
                new SoundObject(nameList.get(82), "Matt", R.raw.learnfrommymistakes),
                new SoundObject(nameList.get(83), "Matt", R.raw.itsbeenawhile),
                new SoundObject(nameList.get(84), "Matt", R.raw.suntreeaok)
        };

        return soundItems;
    }

}
