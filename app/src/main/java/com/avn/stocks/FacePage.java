package com.avn.stocks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avn.stocks.customized.customizedStock;
import com.avn.stocks.receivers.StockAlarmReceiver;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockStats;

public class FacePage extends AppCompatActivity
{
    //////////////////////////////////////// Extras Identifier
    public static final String EXTRA_TICKER = "ext_ticker";
    //////////////////////////////////////// Extras Identifier

    TextView backupText;
    ListView lvMain;
    String[] tempTickerList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_face, menu);
        return true;
    }

    public void launchSettings(MenuItem mi)
    {
        startActivity(new Intent(FacePage.this, SettingsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_page);

        lvMain = (ListView) findViewById(R.id.face_page_lv);
        lvMain.setOnItemClickListener(onTickerClickListener);
        tempTickerList = new String[]
                {
                        "INTC",
                        "TWTR",
                        "MSFT",
                        "TAHO",
                        "JACK",
                        "CASS",
                        "RIGL",
                        "BBSI",
                        "GLDD",
                        "VNET",
                        "BIND",
                        "TBNK",
                        "CHKP",
                        "FLEX",
                        "DXPE",
                        "AMZN",
                        "AVID",
                        "CNOB",
                        "SHLD",
                        "JD",
                        "VTL",
                        "SGMO",
                        "UMPQ",
                        "KURA",
                        "LLTC",
                        "BBEPP",
                        "PLAB",
                        "SQNM",
                        "PKT",
                        "LVNTA",
                        "DTV",
                        "RRD",
                        "KFRC",
                        "MXIM",
                        "LILAK",
                        "PACB",
                        "FFIN",
                        "NCIT",
                        "AMSF",
                        "PEGA",
                        "EMMS",
                        "ELOS",
                        "AREX"
                };
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tempTickerList);
        lvMain.setAdapter(tempAdapter);

        scheduleStockNotifier();

    }

    public void scheduleStockNotifier()
    {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), StockAlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, StockAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                //AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }


    AdapterView.OnItemClickListener onTickerClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            Intent intent = new Intent(FacePage.this, StockSpecifics2.class);
            intent.putExtra(EXTRA_TICKER, tempTickerList[i]);
            startActivity(intent);
        }
    };
}
