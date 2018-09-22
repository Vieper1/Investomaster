package com.avn.stocks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.avn.stocks.fragments.SettingsFragment;

/**
 * Created by Viper GTS on 07-Jan-17.
 */

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment(), "SettingsFragment").commit();
    }
}
