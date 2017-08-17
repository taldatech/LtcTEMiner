package com.example.android.ltcteminer.StratumMiner.Worker;

import com.example.android.ltcteminer.StratumMiner.MiningWork;

/**
 * Created by Tal on 09/08/2017.
 */

public interface IWorkerEvent
{
    public void onNonceFound(MiningWork i_work, int i_nonce);
}
