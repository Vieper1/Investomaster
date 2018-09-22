package com.avn.stocks.customized;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import yahoofinance.Stock;

/**
 * Created by Viper GTS on 06-Jan-17.
 */

public class customizedStock extends Stock
{
    public customizedStock(String symbol)
    {
        super(symbol);
    }


    public String printNow()
    {
        StringBuilder sb = new StringBuilder();
        for (Field f : this.getClass().getDeclaredFields())
        {
            try
            {
                sb.append(f.getName() + ": " + f.get(this));
            }
            catch (IllegalArgumentException ex)
            {
                Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex)
            {
                Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sb.toString();
    }
}
