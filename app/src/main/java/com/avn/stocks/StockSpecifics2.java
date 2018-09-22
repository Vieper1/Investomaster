package com.avn.stocks;

import android.app.DatePickerDialog;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avn.stocks.dbrel.StocksContentProvider;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.GraphViewXML;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockQuotesData;
import yahoofinance.quotes.stock.StockStats;

public class StockSpecifics2 extends AppCompatActivity
{
    private ContentResolver contentResolver;
    private Uri uri;

    String where;
    String[] args;

    private boolean isFavouriteStock;
    private float stockMin;
    private float stockMax;

    private ProgressDialog progressDialog;
    //////////////////////////////////////// For Async Task
    private String stockSymbol;

    private String stockCurrentPrice;


    private List<Float> historicPriceList;

    private Calendar calendar;

    String[] months;
    //////////////////////////////////////// For Async Task






    //////////////////////////////////////// On Layout Items
    private TextView mCurrent;
    private TextView mChange;
    private TextView mOpen;
    private TextView mHigh;
    private TextView mLow;
    private TextView mMktCap;
    private TextView mPERatio;

    private GraphView mGraph;

    private FloatingActionButton fab;

    private LinearLayout lvLimitMin;
    private LinearLayout lvLimitMax;
    private TextView lvLimitMinTxt;
    private TextView lvLimitMaxTxt;
    private String txtMinValue;
    private String txtMaxValue;
    //////////////////////////////////////// On Layout Items

    //////////////////////////////////////// Loops
    long refreshTime;
    Handler h = new Handler(Looper.getMainLooper());
    Runnable r = new Runnable()
    {
        public void run() {
            refreshStocks();
            h.postDelayed(this, refreshTime);
        }
    };
    //////////////////////////////////////// Loops


    private SharedPreferences settings;


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stock_specifics, menu);
        return true;
    }

    public void onRefresh(MenuItem mi)
    {
        refreshStocks();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_specifics2);

        JodaTimeAndroid.init(StockSpecifics2.this);

        stockSymbol = getIntent().getStringExtra(FacePage.EXTRA_TICKER);

        mCurrent = (TextView) findViewById(R.id.stock_current);
        mChange = (TextView) findViewById(R.id.stock_change);
        mOpen = (TextView) findViewById(R.id.stock_open);
        mHigh = (TextView) findViewById(R.id.stock_high);
        mLow = (TextView) findViewById(R.id.stock_low);
        mMktCap = (TextView) findViewById(R.id.stock_mkt_cap);
        mPERatio = (TextView) findViewById(R.id.stock_pe_ratio);

        mGraph = (GraphView) findViewById(R.id.stock_graph);

        lvLimitMin = (LinearLayout) findViewById(R.id.lv_min_stock);
        lvLimitMin.setOnClickListener(onLayoutClickListener);
        lvLimitMax = (LinearLayout) findViewById(R.id.lv_max_stock);
        lvLimitMax.setOnClickListener(onLayoutClickListener);
        lvLimitMinTxt = (TextView) findViewById(R.id.txt_min_stock);
        lvLimitMaxTxt = (TextView) findViewById(R.id.txt_max_stock);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Crunching the latest data...");
        progressDialog.setIndeterminate(true);







        fab = (FloatingActionButton) findViewById(R.id.stock_specifics2_fab);
        fab.setOnClickListener(onFabClickListener);

        contentResolver = getContentResolver();
        uri = Uri.parse("content://com.avn.stocks.provider");

        where = "ticker = ?";
        args = new String[]{getIntent().getStringExtra(FacePage.EXTRA_TICKER)};


        //////////////////////////////////////// Calls
        databaseCall();

        txtMaxValue = lvLimitMaxTxt.getText().toString();
        txtMinValue = lvLimitMinTxt.getText().toString();



        refreshStocks();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        refreshTime = Long.valueOf(settings.getString("pref_refresh_time", "10000"));

        calendar = Calendar.getInstance();
        DateTime dt = new DateTime(calendar);
        dt = dt.minusMonths(1);
        calendar = dt.toCalendar(Locale.getDefault());


        months = new String[]{
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
        };

        updateGraph();
        //////////////////////////////////////// Calls
    }



    private void databaseCall()
    {
        isFavouriteStock = false;

        String[] columns = {StocksContentProvider.TABLE_1_COL_1, StocksContentProvider.TABLE_1_COL_2, StocksContentProvider.TABLE_1_COL_3};
        Cursor cursor = contentResolver.query(uri, columns, where, args, null);

        if(cursor != null)
        {
            cursor.moveToFirst();
            if (cursor.getCount() != 0)
            {
                isFavouriteStock = true;
                fab.setImageResource(R.drawable.svg_favorite_white_48px);
                lvLimitMin.setVisibility(View.VISIBLE);
                lvLimitMax.setVisibility(View.VISIBLE);

                int indexMin = cursor.getColumnIndex(StocksContentProvider.TABLE_1_COL_2);
                int indexMax = cursor.getColumnIndex(StocksContentProvider.TABLE_1_COL_3);


                if(cursor.getFloat(indexMin) != 0.0)
                    lvLimitMinTxt.setText(String.valueOf(cursor.getFloat(indexMin)));

                if(cursor.getFloat(indexMax) != 0.0)
                    lvLimitMaxTxt.setText(String.valueOf(cursor.getFloat(indexMax)));

            }
        }
    }

    @Override
    protected void onPause()
    {
        h.removeCallbacks(r);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        h.postDelayed(r, refreshTime);
        super.onResume();
    }

    ////////////////////////////////////////////////////////////////////// Listeners
    View.OnClickListener onFabClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            isFavouriteStock = !isFavouriteStock;
            ContentValues cv = new ContentValues();

            if (isFavouriteStock) {
                cv.put("ticker", getIntent().getStringExtra(FacePage.EXTRA_TICKER));
                contentResolver.insert(uri, cv);
                Toast.makeText(StockSpecifics2.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.svg_favorite_white_48px);
                lvLimitMin.setVisibility(View.VISIBLE);
                lvLimitMax.setVisibility(View.VISIBLE);
            } else {
                contentResolver.delete(uri, where, args);
                fab.setImageResource(R.drawable.svg_favorite_border_white_48px);
                lvLimitMin.setVisibility(View.INVISIBLE);
                lvLimitMax.setVisibility(View.INVISIBLE);
            }
        }
    };

    LinearLayout.OnClickListener onLayoutClickListener = new LinearLayout.OnClickListener()
    {
        @Override
        public void onClick(final View view)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(StockSpecifics2.this);
            alert.setTitle("Set Limit");
            final EditText input = new EditText(StockSpecifics2.this);
            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint(mCurrent.getText());
            input.setRawInputType(Configuration.KEYBOARD_12KEY);
            alert.setView(input);
            alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    if(input.getText() != null && input.getText().toString() != "" && (Float.valueOf(input.getText().toString()) != 0.0f))
                    {
                        ContentValues cv = new ContentValues();

                        if (view.getId() == R.id.lv_min_stock)
                        {
                            cv.put(StocksContentProvider.TABLE_1_COL_2, input.getText().toString());
                            lvLimitMinTxt.setText(input.getText().toString());
                        }
                        else if (view.getId() == R.id.lv_max_stock)
                        {
                            cv.put(StocksContentProvider.TABLE_1_COL_3, input.getText().toString());
                            lvLimitMaxTxt.setText(input.getText().toString());
                        }
                        contentResolver.update(uri, cv, where, args);

                        updateGraph();
                    }
                    else
                    {
                        Toast.makeText(StockSpecifics2.this, "Invalid value", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
    };

    public void onSetCustomDateListener(View view)
    {
        DatePickerDialog dialog = new DatePickerDialog(StockSpecifics2.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                Calendar userCalendar = Calendar.getInstance();
                userCalendar.set(year, month, day);

                if(userCalendar.before(Calendar.getInstance()))
                {
                    calendar.set(year, month, day);
                    updateGraph();
                }
                else
                    Toast.makeText(StockSpecifics2.this, "Invalid date", Toast.LENGTH_SHORT).show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }
    ////////////////////////////////////////////////////////////////////// Listeners

















    ////////////////////////////////////////////////////////////////////// Async Task
    private void refreshStocks()
    {
        new RefreshAsyncTask().execute();
    }

    private void updateGraph()
    {
        new DeepHistoryAsyncTask().execute();
    }

    private class RefreshAsyncTask extends AsyncTask<String, Void, Void>
    {
        private Stock stock;
        private StockQuote quote;
        private StockStats stats;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings)
        {
            try
            {
                stock = YahooFinance.get(stockSymbol);
                quote = stock.getQuote();
                stats = stock.getStats();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            getSupportActionBar().setTitle(stock.getName());
            String sign = "+";
            if(quote.getChange().floatValue() < 0)
                sign = "";

            mCurrent.setText(quote.getPrice()+"");
            mChange.setText(sign + quote.getChange() + "  " + sign + quote.getChangeInPercent() + "%");
            mOpen.setText(quote.getOpen()+"");
            mHigh.setText(quote.getDayHigh()+"");
            mLow.setText(quote.getDayLow()+"");
            mMktCap.setText(stats.getMarketCap().floatValue()+"");
            mPERatio.setText(stats.getPe()+"");

            progressDialog.dismiss();
        }
    }








    private class DeepHistoryAsyncTask extends AsyncTask<Void, Void, Void>
    {

        private LineGraphSeries<DataPoint> series;
        private LineGraphSeries<DataPoint> seriesMinLimit;
        private LineGraphSeries<DataPoint> seriesMaxLimit;

        private boolean isMinSet = false;
        private boolean isMaxSet = false;

        private List<HistoricalQuote> historicalQuotes;

        @Override
        protected Void doInBackground(Void... voids)
        {
            try
            {



                Stock stock = YahooFinance.get(stockSymbol, calendar, Interval.DAILY);
                historicalQuotes = stock.getHistory();







                DataPoint[] points = new DataPoint[historicalQuotes.size()];

                for(int i=0;i<historicalQuotes.size();i++)
                {
                    points[historicalQuotes.size()-i-1] = new DataPoint(new DateTime(historicalQuotes.get(i).getDate()).toDate(), historicalQuotes.get(i).getClose().floatValue());
                }



                series = new LineGraphSeries<>(points);

                if(!txtMinValue.equals("N/A"))
                {
                    isMinSet = true;
                    seriesMinLimit = new LineGraphSeries<>(new DataPoint[]{
                            new DataPoint(new DateTime(historicalQuotes.get(historicalQuotes.size()-1).getDate()).toDate(), Float.valueOf(txtMinValue)),
                            new DataPoint(new DateTime(historicalQuotes.get(0).getDate()).toDate(), Float.valueOf(txtMinValue))
                    });
                    seriesMinLimit.setColor(Color.parseColor("#EF9A9A"));
                }

                if(!txtMaxValue.equals("N/A"))
                {
                    isMaxSet = true;
                    seriesMaxLimit = new LineGraphSeries<>(new DataPoint[]{
                            new DataPoint(new DateTime(historicalQuotes.get(historicalQuotes.size()-1).getDate()).toDate(), Float.valueOf(txtMaxValue)),
                            new DataPoint(new DateTime(historicalQuotes.get(0).getDate()).toDate(), Float.valueOf(txtMaxValue))
                    });
                    seriesMaxLimit.setColor(Color.parseColor("#A5D6A7"));
                }

                series.setColor(Color.parseColor("#FF4081"));
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            mGraph.removeAllSeries();
            mGraph.addSeries(series);

            if(isMinSet)
            {
                mGraph.addSeries(seriesMinLimit);
            }
            if(isMaxSet)
            {
                mGraph.addSeries(seriesMaxLimit);
            }
//
            mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double v, boolean b) {
                    DateTime dt = new DateTime((long)v);
                    if(b)
                    {
//                        return months[dt.getMonthOfYear()-1]+" "+dt.getDayOfMonth();
                        return dt.getDayOfMonth()+"";
                    }
                    else
                        return super.formatLabel(v, b);
                }
            });
            mGraph.getGridLabelRenderer().setNumHorizontalLabels(10); // only 4 because of the space
//
//    // set manual x bounds to have nice steps

            mGraph.getViewport().setMinX(historicalQuotes.get(historicalQuotes.size()-1).getDate().getTimeInMillis());
            mGraph.getViewport().setMaxX(historicalQuotes.get(0).getDate().getTimeInMillis());


            mGraph.getViewport().setScalableY(true);
            mGraph.getViewport().setScalable(true);

//            mGraph.getGridLabelRenderer().setVerticalAxisTitle("Stock value");
//            mGraph.getGridLabelRenderer().setHorizontalAxisTitle("Day of month");


            if(isMinSet) {
                mGraph.getViewport().setMinY(Math.min(series.getLowestValueY(), Float.valueOf(txtMinValue)));
                mGraph.getViewport().setMaxY(Math.max(series.getHighestValueY(), Float.valueOf(txtMinValue)));
            }
            else
                mGraph.getViewport().setMinY(series.getLowestValueY());



            if(isMaxSet) {
                mGraph.getViewport().setMaxY(Math.max(series.getHighestValueY(), Float.valueOf(txtMaxValue)));
                mGraph.getViewport().setMinY(Math.min(series.getLowestValueY(), Float.valueOf(txtMaxValue)));
            }
            else
                mGraph.getViewport().setMaxY(series.getHighestValueY());










            mGraph.getViewport().setXAxisBoundsManual(true);
            mGraph.getViewport().setYAxisBoundsManual(true);
//
//    // as we use dates as labels, the human rounding to nice readable numbers
//    // is not necessary
//            mGraph.getGridLabelRenderer().setHumanRounding(true);
//            mGraph.getViewport();
        }
    }
    ////////////////////////////////////////////////////////////////////// Async Task
}