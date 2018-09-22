package com.avn.stocks.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.avn.stocks.FacePage;
import com.avn.stocks.R;
import com.avn.stocks.dbrel.StocksContentProvider;

import java.io.IOException;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by Viper GTS on 08-Jan-17.
 */

public class StockNotifierService
        extends IntentService
{
    private static ContentResolver contentResolver;
    private static Uri uri;
    private static String where;

    public StockNotifierService() {
        super("MyTestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        contentResolver = getContentResolver();
        uri = Uri.parse("content://com.avn.stocks.provider");

        where = StocksContentProvider.TABLE_1_COL_2+ " IS NOT NULL OR " + StocksContentProvider.TABLE_1_COL_3 + " IS NOT NULL";

        String[] columns = {StocksContentProvider.TABLE_1_COL_1, StocksContentProvider.TABLE_1_COL_2, StocksContentProvider.TABLE_1_COL_3};
        Cursor cursor = contentResolver.query(uri, columns, where, null, null);

        String finalMessage = "";

        if(cursor != null)
        {
            cursor.moveToFirst();
            if(cursor.getCount() > 0)
            {
                System.out.println("//////////////////////////////////////////////////////////////////////// Service Call");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle("Investomaster")
                        .setSmallIcon(R.drawable.svg_timeline_white_48px)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                Notification notification;
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                Intent launchFacePage = new Intent(getApplicationContext(), FacePage.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchFacePage, 0);

                String[] tickersToCheck = new String[cursor.getCount()];
                float[] minLimit = new float[cursor.getCount()];
                float[] maxLimit = new float[cursor.getCount()];

                int tickerIndex = cursor.getColumnIndex(StocksContentProvider.TABLE_1_COL_1);
                int minIndex = cursor.getColumnIndex(StocksContentProvider.TABLE_1_COL_2);
                int maxIndex = cursor.getColumnIndex(StocksContentProvider.TABLE_1_COL_3);

                for (int i=0; i<cursor.getCount(); i++)
                {
                    tickersToCheck[i] = cursor.getString(tickerIndex);
                    minLimit[i] = cursor.getFloat(minIndex);
                    maxLimit[i] = cursor.getFloat(maxIndex);
                    cursor.moveToNext();
                }

                try
                {
                    Map<String, Stock> stockResults = YahooFinance.get(tickersToCheck);

                    for(int i=0; i<stockResults.size(); i++)
                    {
                        Stock stockFromTicker = stockResults.get(tickersToCheck[i]);
                        float currentPrice = stockFromTicker.getQuote().getPrice().floatValue();
                        if(minLimit[i] != 0 && currentPrice < minLimit[i])
                        {
//                            sbLow.append(tickersToCheck[i]+"\n");
                            finalMessage += tickersToCheck[i] + " is below limit\n";
                        }

                        if(maxLimit[i] != 0 && currentPrice > maxLimit[i])
                        {
//                            sbHigh.append(tickersToCheck[i]+"\n");
                            finalMessage += tickersToCheck[i] + " is above limit\n";
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
//                    if(!sbLow.toString().equals(""))
//                    {
//                        finalMessage += "Stocks that have fallen below limit:\n" + sbLow.toString();
//                        sbFinal.append("Stocks that have fallen below limit:\n");
//                        sbFinal.append(sbLow.toString());
//                    }
//
//                    if(!sbHigh.toString().equals(""))
//                    {
//                        finalMessage += "Stocks that have gone above limit:\n" + sbHigh.toString();
//                        sbFinal.append("Stocks that have gone above limit:\n");
//                        sbFinal.append(sbHigh.toString());
//                    }

                    System.out.println("//////////////////////////////////////////////////////////////////////// Service Call2");

                    Log.i("Investomaster", "Stock checker service");

                    //if(!(sbLow.toString()+sbHigh.toString()).equals(""))
                    if(!finalMessage.equals(""))
                    {
                        finalMessage = finalMessage.substring(0, finalMessage.length()-1);
//                        builder.setContentText(finalMessage).setContentIntent(pendingIntent);
                        System.out.println("//////////////////////////////");
                        System.out.println(finalMessage);
                        System.out.println("//////////////////////////////");
                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(finalMessage));
                        builder.setContentText("Expand to view stock changes");
                        notification = builder.build();
                        notification.flags = Notification.FLAG_ONGOING_EVENT;
                        manager.notify(13, notification);
                    }
                }
            }
        }
        stopSelf();
    }
}
