package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.HexArray;
import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.fasterxml.jackson.databind.JsonNode;

import static android.media.CamcorderProfile.get;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJsonResultSubscribe extends StratumJsonResult {
    public final String session_id;
    public final HexArray xnonce1;
    public final int xnonce2_size;
    public final static String TEST_PATT ="{\"id\":1,\"result\":[[\"mining.notify\",\"b86c07fd6cc70b367b61669fb5e91bfa\"],\"f8000105\",4],\"error\":null}";
    public StratumJsonResultSubscribe(JsonNode i_json_node) throws MinyaException {
        super(i_json_node);
        //Check for errors:
        if(this.error!=null){
            throw new MinyaException(this.error.asText());
        }
        JsonNode n = i_json_node.get("result");
        if (!n.isArray()) {
            throw new MinyaException();
        }
        // sessionID
        if  ( (n.get(0) != null ) &&(n.get(0).get(1) != null ) && (n.get(0).get(1).get(0) != null )
                && (n.get(0).get(1).get(0).asText().compareTo("mining.notify") != 0)) {
            //Not good
        }
        else if ( (n.get(0) != null ) && (n.get(0).get(0) != null ) && (n.get(0).get(0).get(0) != null )
                && (n.get(0).get(0).get(0).asText().compareTo("mining.notify") != 0)) {
            //Not good
        }
        else if ( (n.get(0) != null ) && (n.get(0).get(0) != null ) && (n.get(0).get(0).get(0) != null )
                && (n.get(0).get(0).get(0).asText().compareTo("\"mining.notify\"") != 0)) {
            //Not good
        }
        else if ( (n.get(0) != null ) && (n.get(0).get(0) != null ) && (n.get(0).get(0).get(0) != null )
                && (n.get(0).get(0).get(0).get(0) != null ) && (n.get(0).get(0).get(0).get(0).asText().compareTo("mining.notify") != 0)) {
            //Not good
        }
        else if ( (n.get(0) != null)  && ((n.get(0).get(0) != null)
                && (n.get(0).get(0).asText().compareTo("mining.notify") != 0))) {
            throw new MinyaException();
        }
//        if ( ((n.get(0).get(1).get(0) != null ) && (n.get(0).get(1).get(0).asText().compareTo("mining.notify") != 0) ) &&
//                ((n.get(0).get(0).get(0) != null ) && (n.get(0).get(0).get(0).asText().compareTo("mining.notify") != 0)) &&
//                ((n.get(0).get(0) != null) && (n.get(0).get(0).asText().compareTo("mining.notify") != 0)) ) {
//            throw new MinyaException();
//        }

//        if ((n.get(0).get(1).get(0) != null )) {
//            if (n.get(0).get(1).get(0).asText().compareTo("mining.notify") != 0) {
//                throw new MinyaException();
//            }
//        }
//        else if ((n.get(0).get(0).get(0) != null )) {
//            if (n.get(0).get(0).get(0).asText().compareTo("mining.notify") != 0) {
//                throw new MinyaException();
//            }
//        } else {
//            if (n.get(0).get(0).asText().compareTo("mining.notify") != 0) {
//                throw new MinyaException();
//            }
//        }
        if (n.get(0).get(1) != null) {
            this.session_id = n.get(0).get(1).asText();
        }
        else if (n.get(0).get(0).get(1) != null) {
            this.session_id = n.get(0).get(0).get(1).asText();
        }
        else if (n.get(0).get(0).get(0).get(1) != null) {
            this.session_id = n.get(0).get(0).get(0).get(1).asText();
        } else {
            this.session_id = "0";
        }

        // xnonce1
        this.xnonce1 = new HexArray(n.get(1).asText());
        //xnonce2_size
        this.xnonce2_size = n.get(2).asInt();
        return;
    }
}