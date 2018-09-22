package com.avn.stocks.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.avn.stocks.services.StockNotifierService;

/**
 * Created by Viper GTS on 08-Jan-17.
 */

public class StockAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.avn.stocks.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, StockNotifierService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
