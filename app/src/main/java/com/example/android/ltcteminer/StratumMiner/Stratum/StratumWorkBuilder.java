package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.HexArray;
import com.example.android.ltcteminer.StratumMiner.MiningWork;
import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.example.android.ltcteminer.StratumMiner.StratumMiningWork;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumWorkBuilder
{
    private StratumJsonResultSubscribe _subscribe=null;
    private StratumJsonMethodMiningNotify _notify=null;
    private HexArray _xnonce2;
    private HexArray _coinbase;
    private HexArray _merkle_loot;
    private double _difficulty=Double.NEGATIVE_INFINITY;
    private byte[] sha256d(byte[] i_s) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(md.digest(i_s));
    }
    public StratumWorkBuilder(StratumJsonResultSubscribe i_attached_subscribe)
    {
        this._subscribe=i_attached_subscribe;
    }
    public void setDiff(StratumJsonMethodSetDifficulty i_difficulty) throws MinyaException
    {
        this._difficulty=i_difficulty.difficulty;
    }
    public void setNotify(StratumJsonMethodMiningNotify i_attached_notify) throws MinyaException
    {
        this._notify=i_attached_notify;
        try{
            //複製
            this._xnonce2=this._notify.getXnonce2(this._subscribe);
            this._coinbase=this._notify.getCoinbase(this._subscribe);
			/*
			//Generate merkle root
			sha256d(merkle_root, sctx->job.coinbase, sctx->job.coinbase_size);
			for (i = 0; i < sctx->job.merkle_count; i++) {
				memcpy(merkle_root + 32, sctx->job.merkle[i], 32);
				sha256d(merkle_root, merkle_root, 64);
			}
			*/
            byte[] merkle_loot=new byte[64];
            System.arraycopy(sha256d(this._coinbase.refHex()),0,merkle_loot,0,32);
            for(int i=0;i<this._notify.merkle_arr.length;i++){
                System.arraycopy(this._notify.merkle_arr[i].refHex(),0,merkle_loot,32,32);
                System.arraycopy(sha256d(merkle_loot),0,merkle_loot,0,32);
            }
            this._merkle_loot=new HexArray(merkle_loot);
            this._merkle_loot.swapEndian();
        }catch(NoSuchAlgorithmException e){
            throw new MinyaException(e);
        }
    }
    public HexArray refXnonce2()
    {
        return this._xnonce2;
    }

    public MiningWork buildMiningWork() throws MinyaException
    {
        if(this._notify==null ||this._subscribe==null || this._difficulty<0){
            return null;
        }
        //Increment extranonce2
        HexArray xnonce2=this._xnonce2;
        String xnonce2_str=xnonce2.getStr();
        for (int i = 0; i < xnonce2.getLength() && (0==(++xnonce2.refHex()[i])); i++);

        //Assemble block header
        HexArray work_data=new HexArray(this._notify.version);
        work_data.append(this._notify.prev_hash);
        work_data.append(this._merkle_loot,0,32);
        work_data.append(this._notify.ntime);
        work_data.append(this._notify.nbit);
        work_data.append(new byte[]{
                0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,(byte)0x80});
        work_data.append(new byte[40]);
        work_data.append(new byte[]{
                (byte)0x80,0x02,0x00,0x00});
        return new StratumMiningWork(work_data,diff2target(this._difficulty/65536.0),this._notify.job_id,xnonce2_str);

    }
    private static HexArray diff2target(double diff)
    {
        long m;
        int k;
        byte[] target=new byte[8*4];
        for (k = 6; k > 0 && diff > 1.0; k--){
            diff /= 4294967296.0;
        }
        m = (long)(4294901760.0 / diff);
        if (m == 0 && k == 6){
            Arrays.fill(target,(byte)0xff);
        }else{
            Arrays.fill(target,(byte)0);
            for(int i=0;i<8;i++){
                target[k*4+i]=(byte)((m>>(i*8))&0xff);
            }
        }
        return new HexArray(target);
    }



    public static void main(String[] args)
    {
        try {
            String SR="{\"error\": null, \"id\": 1, \"result\": [[\"mining.notify\", \"ae6812eb4cd7735a302a8a9dd95cf71f\"], \"f801d02f\", 4]}";
            String NT="{\"params\": [\"8bf\", \"8e50f956acdabb3f8e981a4797466043021388791bfa70b1c1a1ba54a8fbdf50\", \"01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff26022d53062f503253482f042e4cb55208\", \"0d2f7374726174756d506f6f6c2f0000000001310cb3fca45500001976a91446a9148895dfa88b9e1596c14afda26b9071861488ac00000000\", [\"e5af4fcc527ce0aecc36848b550032e10f1359a16a3d6b07d76d0ccd361a66e3\", \"9bb81dcf2f43dafcdb6006353ce628a81e27bafd9ea13142bbce9cdf2fa0319b\", \"d7c70d31366c6068a5481f1a9fd786ee1aaa5032c3abd87ac9e024279cd21432\", \"af8cf8b535246110f17fc823c380d1c129d72d20db91cc7be4fb99d5327234e7\", \"5ed74269c86186c14bed42d020fceb631c2db2ad46de19d593f85888503a4c3d\", \"9243decb3a9a34360650f9132cdfca33bbc26677a3c079e978d7c455303861ee\", \"6c7c245323e51b1cda9e7f9de1917c36f94a09d06726352b952c7bf18e5f0dd1\", \"cad9bdc3937c263d78879ccb935cde980f43469260dd0a23d0f0ad9d16fe1b89\", \"b13743cd86b181d1cf8de23404951e4b0c39a273e2061f1a418c36d51dfd3be9\"], \"00000001\", \"1c00adb7\", \"52b54c29\", true], \"id\": null, \"method\": \"mining.notify\"}";
            String ST="{\"params\": [128], \"id\": null, \"method\": \"mining.set_difficulty\"}";
            String WORK_DATA="000000018e50f956acdabb3f8e981a4797466043021388791bfa70b1c1a1ba54a8fbdf5093b73998a3b9d1ad9ee12578b6ffb49088bb9321fcb159e15f10b397cb514e4952b54c291c00adb700000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000";
            String WORK_TARGET="000000000000000000000000000000000000000000000000000000feff010000";
            ObjectMapper mapper = new ObjectMapper();
            StratumJsonResultSubscribe s=new StratumJsonResultSubscribe(mapper.readTree(SR));
            StratumJsonMethodMiningNotify n=new StratumJsonMethodMiningNotify(mapper.readTree(NT));
            StratumJsonMethodSetDifficulty d=new StratumJsonMethodSetDifficulty(mapper.readTree(ST));
            StratumWorkBuilder j=new StratumWorkBuilder(s);
            j.setNotify(n);
            j.setDiff(d);
            MiningWork w=j.buildMiningWork();
            w.dump();
            System.out.println(WORK_DATA);
            System.out.println(WORK_TARGET);

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MinyaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
