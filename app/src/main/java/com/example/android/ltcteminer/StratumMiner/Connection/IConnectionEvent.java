package com.example.android.ltcteminer.StratumMiner.Connection;

import com.example.android.ltcteminer.StratumMiner.MiningWork;

/**
 * Created by Tal on 09/08/2017.
 */

public interface IConnectionEvent
{
    public void onNewWork(MiningWork i_new_work);
    public void onSubmitResult(MiningWork i_listener,int i_nonce,boolean i_result);

}