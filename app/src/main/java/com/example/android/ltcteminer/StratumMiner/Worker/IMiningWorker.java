package com.example.android.ltcteminer.StratumMiner.Worker;

import com.example.android.ltcteminer.StratumMiner.MiningWork;
import com.example.android.ltcteminer.StratumMiner.MinyaException;

/**
 * Created by Tal on 09/08/2017.
 */

public interface IMiningWorker
{
    public enum Notification {
        SYSTEM_ERROR,
        PERMISSION_ERROR,
        CONNECTION_ERROR,
        AUTHENTICATION_ERROR,
        COMMUNICATION_ERROR,
        LONG_POLLING_FAILED,
        LONG_POLLING_ENABLED,
        CONNECTING,
        NEW_BLOCK_DETECTED,
        SPEED,
        NEW_WORK,
        POW_TRUE,
        POW_FALSE,
        TERMINATED
    };

    public boolean doWork(MiningWork i_work) throws MinyaException;

    public void stopWork() throws MinyaException;

    public int getProgress();

    public long getNumberOfHash();

    public void addListener(IWorkerEvent i_listener) throws MinyaException;
}