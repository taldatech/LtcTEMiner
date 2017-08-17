package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.HexArray;
import com.example.android.ltcteminer.StratumMiner.MinyaException;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJson
{
    protected StratumJson()
    {
        return;
    }

    protected StratumJson(StratumJson i_src)
    {
        return;
    }
    protected static HexArray toHexArray(String i_str, int i_str_len) throws MinyaException
    {
        if (i_str.length() != i_str_len) {
            throw new MinyaException();
        }
        return new HexArray(i_str);
    }
}