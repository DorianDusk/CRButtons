package com.melothemelon.crbuttons;

import android.provider.BaseColumns;

public class DatabaseSchema {

    public static abstract class MainTable implements BaseColumns{
        public static final String TABLE_NAME = "main_table";
        public static final String NAME = "name";
        public static final String CHARACTERNAME = "charactername";
        public static final String RESOURCE_ID = "resourceID";
    }

    public static abstract class FavoritesTable implements BaseColumns{
        public static final String TABLE_NAME = "favorites_table";
        public static final String NAME = "name";
        public static final String CHARACTERNAME = "charactername";
        public static final String RESOURCE_ID = "resourceID";
    }


}
