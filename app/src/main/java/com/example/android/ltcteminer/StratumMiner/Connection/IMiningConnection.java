package com.example.android.ltcteminer.StratumMiner.Connection;

import com.example.android.ltcteminer.StratumMiner.MiningWork;
import com.example.android.ltcteminer.StratumMiner.MinyaException;

/**
 * Created by Tal on 09/08/2017.
 */

public interface IMiningConnection
{
    public void addListener(IConnectionEvent i_listener) throws MinyaException;
    public MiningWork connect() throws MinyaException;
    //    public void connect() throws MinyaException;
    public void disconnect() throws MinyaException;
    public MiningWork getWork() throws MinyaException;
    public void submitWork(MiningWork i_work, int i_nonce) throws MinyaException;
//    public boolean submitWork(MiningWork i_work, int i_nonce) throws MinyaException;
}