package com.avn.stocks.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.avn.stocks.R;

/**
 * Created by Viper GTS on 07-Jan-17.
 */

public class SettingsFragment
        extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
