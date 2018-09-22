package com.avn.stocks.dbrel;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Viper GTS on 06-Jan-17.
 */

public class StocksContentProvider
        extends ContentProvider
{
    // v1.0
    private static final String DATABASE_NAME = "StocksInfo.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;


    // TABLE NAMES
    public static final String TABLE_0_NAME = "current";
    public static final String TABLE_1_NAME = "favorites";


    // TABLE 0 - Current
    public static final String TABLE_0_COL_0 = "_id";
    public static final String TABLE_0_COL_1 = "ticker";

    // TABLE 1 - Favs
    public static final String TABLE_1_COL_0 = "_id";
    public static final String TABLE_1_COL_1 = "ticker";
    public static final String TABLE_1_COL_2 = "min";
    public static final String TABLE_1_COL_3 = "max";


    // Create Queries
    // v1.0
    private final String CREATE_TABLE_0_QUERY = String.format(
            "CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT NOT NULL);",

            TABLE_0_NAME,

            TABLE_0_COL_0,
            TABLE_0_COL_1
    );

    private final String CREATE_TABLE_1_QUERY = String.format(
            "CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT NOT NULL, %s REAL, %s REAL);",

            TABLE_1_NAME,

            TABLE_1_COL_0,
            TABLE_1_COL_1,
            TABLE_1_COL_2,
            TABLE_1_COL_3
    );














    @Override
    public boolean onCreate() {
        StocksConnectionHelper helper = new StocksConnectionHelper(getContext());
        db = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return db.query(TABLE_1_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        if(db.insert(TABLE_1_NAME, null, values) == -1)
            throw new RuntimeException("Error while saving lyrics");
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return db.delete(TABLE_1_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return db.update(TABLE_1_NAME, values, selection, selectionArgs);
    }


    class StocksConnectionHelper
        extends SQLiteOpenHelper{
        public StocksConnectionHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            //db.execSQL(CREATE_TABLE_0_QUERY);
            db.execSQL(CREATE_TABLE_1_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_0_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_1_NAME);
            onCreate(db);
        }
    }
}
