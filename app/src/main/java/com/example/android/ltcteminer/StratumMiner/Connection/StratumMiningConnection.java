package com.example.android.ltcteminer.StratumMiner.Connection;

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.ltcteminer.StratumMiner.MiningWork;
import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.example.android.ltcteminer.StratumMiner.MinyaLog;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJson;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonMethodGetVersion;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonMethodMiningNotify;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonMethodReconnect;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonMethodSetDifficulty;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonMethodShowMessage;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonResult;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonResultStandard;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumJsonResultSubscribe;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumSocket;
import com.example.android.ltcteminer.StratumMiner.Stratum.StratumWorkBuilder;
import com.example.android.ltcteminer.StratumMiner.StratumMiningWork;
import com.example.android.ltcteminer.StratumMiner.Worker.IMiningWorker;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.example.android.ltcteminer.StratumMiner.Constants.CLIENT_NAME_STRING;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumMiningConnection extends Observable implements IMiningConnection
{
    private class SubmitOrder
    {
        public SubmitOrder(long i_id, StratumMiningWork i_work, int i_nonce)
        {
            this.id=i_id;
            this.work=i_work;
            this.nonce=i_nonce;
            return;
        }
        public final long id;
        public final MiningWork work;
        public final int nonce;
    }

    private class AsyncRxSocketThread extends Thread
    {
        private ArrayList<SubmitOrder> _submit_q=new ArrayList<SubmitOrder>();
        private ArrayList<StratumJson> _json_q=new ArrayList<StratumJson>();
        private StratumMiningConnection _parent;
        public AsyncRxSocketThread(StratumMiningConnection i_parent) throws SocketException
        {
            this._parent=i_parent;
            this._parent._sock.setSoTimeout(100);
        }
        public void run()
        {
//            try {
//                _parent._sock = new StratumSocket(_parent._server);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                this._parent._sock.setSoTimeout(100);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
            for(;;){
                try {
                    StratumJson json=this._parent._sock.recvStratumJson();
                    if(json==null){
                        Thread.sleep(1);
                        continue;
                    }
                    this.onJsonRx(json);
                } catch (SocketTimeoutException e){
                    if(this.isInterrupted()){
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e){

                    break;
                }
            }
        }

        /**
         *JSON Parsing
         */
        private void onJsonRx(StratumJson i_json)
        {
            Class<?> iid=i_json.getClass();
            MinyaLog.debug("onJsonRx:"+iid.getName());
            Log.d("onJsonRx:",iid.getName());

            if(iid==StratumJsonMethodGetVersion.class)
            {
            }else if(iid==StratumJsonMethodMiningNotify.class)
            {
                this._parent.cbNewMiningNotify((StratumJsonMethodMiningNotify)i_json);
            }else if(iid==StratumJsonMethodReconnect.class){
            }else if(iid==StratumJsonMethodSetDifficulty.class)
            {
                this._parent.cbNewMiningDifficulty((StratumJsonMethodSetDifficulty) i_json);
            }else if(iid==StratumJsonMethodShowMessage.class){
            }else if(iid==StratumJsonResultStandard.class)
            {

                {
                    StratumJsonResultStandard sjson=(StratumJsonResultStandard)i_json;
                    SubmitOrder so=null;
                    synchronized(this._submit_q){
                        for(SubmitOrder i: this._submit_q){
                            if(i.id==sjson.id){
                                //submit_q
                                this._submit_q.remove(i);
                                so=i;
                                break;
                            }
                        }
                    }
                    if(so!=null){
                        this._parent.cbSubmitRecv(so,sjson);
                    }
                }
                synchronized(this._json_q)
                {
                    this._json_q.add(i_json);
                }
                this.semaphore.release();
            }else if(iid==StratumJsonResultSubscribe.class)
            {
                synchronized(this._json_q)
                {
                    this._json_q.add(i_json);
                }
                this.semaphore.release();
            }
            return;
        }
        private Semaphore semaphore = new Semaphore(0);
        /**
         * JSON Result
         */
        public StratumJson waitForJsonResult(long i_id,Class<?> i_class,int i_wait_for_msec)
        {
            long time_out=i_wait_for_msec;
            do{
                long s=System.currentTimeMillis();
                try {
                    if(!semaphore.tryAcquire(time_out, TimeUnit.MILLISECONDS)){
                        return null;
                    }
                } catch (InterruptedException e) {
                    return null;
                }
                synchronized(this._json_q)
                {

                    for(StratumJson json : this._json_q){
                        if(!(json.getClass() == i_class)){
                            continue;
                        }
                        StratumJsonResult jr=(StratumJsonResult)json;
                        if(jr.id==null){
                            continue;
                        }
                        if(jr.id!=i_id){
                            continue;
                        }
                        this._json_q.remove(json);
                        return json;
                    }
                }
                time_out-=(System.currentTimeMillis()-s);
            }while(time_out>0);
            return null;
        }
        /**
         * Submit ID
         */
        public void addSubmitOrder(SubmitOrder i_submit_id)
        {
            synchronized(this._submit_q){
                this._submit_q.add(i_submit_id);
            }
        }
    }

    private static class SocketParams {
        StratumSocket _sock;
        URI _server;

        SocketParams (StratumSocket sock,URI server) {
            this._sock=sock;
            this._server=server;
        }
    }

//    public interface AsyncResponse {
//        void proccessFinish(StratumSocket sock_res);
//    }

    public class SocketConnectAsyncTask extends AsyncTask<SocketParams,Void,StratumSocket> {

//        public AsyncResponse delegate=null;

//        @Override
//        protected void onPostExecute(StratumSocket sock_res) {
//            delegate.proccessFinish(sock_res);
//        }

        @Override
        protected StratumSocket doInBackground(SocketParams... params) {
            try {
                params[0]._sock = new StratumSocket(params[0]._server);
                return params[0]._sock;
            } catch (IOException e) {
                setChanged();
                notifyObservers(IMiningWorker.Notification.CONNECTION_ERROR);
                e.printStackTrace();
                return null;
            }
        }
    }


    private final String CLIENT_NAME=CLIENT_NAME_STRING;
    private String _uid;
    private String _pass;
    private URI _server;
    private StratumSocket _sock=null;
    private AsyncRxSocketThread _rx_thread;

    public StratumMiningConnection(String i_url, String i_userid, String i_password) throws MinyaException
    {
        this._pass=i_password;
        this._uid=i_userid;
        try {
            this._server=new URI(i_url);
        } catch (URISyntaxException e) {
            throw new MinyaException(e);
        }
    }
    private StratumJsonMethodSetDifficulty _last_difficulty=null;
    private StratumJsonMethodMiningNotify _last_notify=null;

    /**
     * Server Connection
     * @throws MinyaException
     */
    public MiningWork connect() throws MinyaException
    {
        setChanged();
        notifyObservers(IMiningWorker.Notification.CONNECTING);
        //Connect to host
        try {
            MiningWork ret=null;
            SocketParams sock_par = new SocketParams(this._sock,this._server);
            SocketConnectAsyncTask sock_task = new SocketConnectAsyncTask();
            try {
                this._sock = sock_task.execute(sock_par).get();
            } catch (InterruptedException e) {
                setChanged();
                notifyObservers(IMiningWorker.Notification.CONNECTION_ERROR);
                e.printStackTrace();
            } catch (ExecutionException e) {
                setChanged();
                notifyObservers(IMiningWorker.Notification.CONNECTION_ERROR);
                e.printStackTrace();
            }

            //this._sock = new StratumSocket(this._server);
            this._rx_thread = new AsyncRxSocketThread(this);
            this._rx_thread.start();
            //3回トライ
            int i;

            //subscribe
            StratumJsonResultSubscribe subscribe=null;
            {
                for(i=0;i<3;i++){
                    MinyaLog.message("Request Stratum subscribe...");
                    Log.d("StratumMiningConnection","Request Stratum subscribe");
                    subscribe=(StratumJsonResultSubscribe)this._rx_thread.waitForJsonResult(this._sock.subscribe(CLIENT_NAME),StratumJsonResultSubscribe.class,3000);
                    if(subscribe==null || subscribe.error!=null){
                        MinyaLog.warning("Stratum subscribe error.");
                        Log.w("StratumMiningConnection","Stratum subscribe error.");
                        continue;
                    }
                    break;
                }
                if(i==3){
                    throw new MinyaException("Stratum subscribe error.");
                }
            }

            //Authorize and make  a 1st work.
            for(i=0;i<3;i++){
                MinyaLog.message("Request Stratum authorize...");
                Log.d("StratumMiningConnection","Request Stratum authorize...");
                StratumJsonResultStandard auth=(StratumJsonResultStandard) this._rx_thread.waitForJsonResult(this._sock.authorize(this._uid,this._pass), StratumJsonResultStandard.class,3000);
                if(auth==null || auth.error!=null){
                    MinyaLog.warning("Stratum authorize error.");
                    Log.w("StratumMiningConnection","Stratum authorize error.");
                    continue;
                }
                if(!auth.result){
                    MinyaLog.warning("Stratum authorize result error.");
                    Log.w("StratumMiningConnection","Stratum authorize result error.");
                }
                synchronized(this._data_lock){
                    //worker builder:
                    this._work_builder=new StratumWorkBuilder(subscribe);
                    //Difficulty check:
                    if(this._last_difficulty!=null){
                        this._work_builder.setDiff(this._last_difficulty);
                    }
                    if(this._last_notify!=null){
                        this._work_builder.setNotify(this._last_notify);
                    }
                    ret=this._work_builder.buildMiningWork();
                }
                //Complete!
                MinyaLog.message("Stratum authorize complete!");
                Log.i("StratumMiningConnection","Stratum authorize complete!");
                return ret;
            }
            setChanged();
            notifyObservers(IMiningWorker.Notification.AUTHENTICATION_ERROR);
            throw new MinyaException("Stratum authorize process failed.");
        } catch (UnknownHostException e){
            setChanged();
            notifyObservers(IMiningWorker.Notification.CONNECTION_ERROR);
            throw new MinyaException(e);
        } catch (IOException e) {
            throw new MinyaException(e);
        }
    }

    public void disconnect() throws MinyaException
    {
        try {
            //threadの停止
            this._rx_thread.interrupt();
            this._rx_thread.join();
            //Socketの停止
            this._sock.close();
            setChanged();
            notifyObservers(IMiningWorker.Notification.TERMINATED);
            synchronized(this._data_lock)
            {
                this._work_builder=null;
            }
        } catch (IOException e) {
            throw new MinyaException(e);
        } catch (InterruptedException e) {
            throw new MinyaException(e);
        }
    }
    private Object _data_lock=new Object();
    private StratumWorkBuilder _work_builder=null;


    public MiningWork getWork()
    {
        MiningWork work=null;
        synchronized(this._data_lock)
        {
            if(this._work_builder==null){
                return null;
            }
            try {
                work=this._work_builder.buildMiningWork();
            } catch (MinyaException e){
                return null;
            }
        }
        return work;
    }
    private ArrayList<IConnectionEvent> _as_listener=new ArrayList<IConnectionEvent>();

    public void addListener(IConnectionEvent i_listener)
    {
        this._as_listener.add(i_listener);
        return;
    }

    /**
     * Thread:
     */
    private void cbNewMiningNotify(StratumJsonMethodMiningNotify i_notify)
    {
        synchronized(this._data_lock)
        {
            if(this._work_builder==null){
                this._last_notify=i_notify;
                return;
            }
        }
        //notify:
        try {
            MinyaLog.message("Receive new job:"+i_notify.job_id);
            Log.i("StratumMiningConnection","Receive new job:"+i_notify.job_id);
            setChanged();
            notifyObservers(IMiningWorker.Notification.NEW_WORK);
            this._work_builder.setNotify(i_notify);
        } catch (MinyaException e){
            MinyaLog.debug("Catch Exception:\n"+e.getMessage());
            Log.d("StratumMiningConnection","Catch Exception:\n"+e.getMessage());
        }
        MiningWork w=this.getWork();
        if(w==null){
            return;
        }
        for(IConnectionEvent i: this._as_listener){
            i.onNewWork(w);
        }
    }
    private void cbNewMiningDifficulty(StratumJsonMethodSetDifficulty i_difficulty)
    {
        MinyaLog.message("Receive set difficulty:"+i_difficulty.difficulty);
        Log.i("StratumMiningConnection","Receive set difficulty:"+i_difficulty.difficulty);
        synchronized(this._data_lock)
        {
            if(this._work_builder==null){
                this._last_difficulty=i_difficulty;
                return;
            }
        }
        //notify:
        try {
            this._work_builder.setDiff(i_difficulty);
        } catch (MinyaException e) {
            MinyaLog.debug("Catch Exception:\n"+e.getMessage());
            Log.d("StratumMiningConnection","Catch Exception:\n"+e.getMessage());
        }
        MiningWork w=this.getWork();
        if(w==null){
            return;
        }
        for(IConnectionEvent i: this._as_listener){
            i.onNewWork(w);
        }
    }

    private void cbSubmitRecv(SubmitOrder so, StratumJsonResultStandard i_result)
    {
        MinyaLog.message("SubmitResponse "+so.nonce+" ["+(i_result.result?"Accepted":"Rejected")+"]");
        Log.i("StratumMiningConnection","SubmitResponse "+so.nonce+" ["+(i_result.result?"Accepted":"Rejected")+"]");
        for(IConnectionEvent i: this._as_listener){
            i.onSubmitResult(so.work,so.nonce,i_result.error==null);
        }
    }
    public void submitWork(MiningWork i_work, int i_nonce) throws MinyaException
    {
        if(!(i_work instanceof StratumMiningWork)){
            throw new MinyaException();
        }
        StratumMiningWork w=(StratumMiningWork)i_work;
        String ntime=w.data.getStr(StratumMiningWork.INDEX_OF_NTIME,4);
        //Stratum:
        try {
            long id=this._sock.submit(i_nonce,this._uid,w.job_id, w.xnonce2, ntime);
            SubmitOrder so=new SubmitOrder(id,w,i_nonce);
            this._rx_thread.addSubmitOrder(so);
            MinyaLog.message("Found! "+so.nonce+" Request submit("+w.job_id+")");
            Log.i("StratumMiningConnection","Found! "+so.nonce+" Request submit("+w.job_id+")");
        } catch (IOException e) {
            throw new MinyaException(e);
        }
    }

}
