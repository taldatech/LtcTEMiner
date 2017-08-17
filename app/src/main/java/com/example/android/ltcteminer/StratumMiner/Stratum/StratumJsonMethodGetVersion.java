package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJsonMethodGetVersion extends StratumJsonMethod
{
    //{"method":"client.reconnect",params:["host",1]}
    public final static String TEST_PATT = "{\"params\": [], \"jsonrpc\": \"2.0\", \"method\": \"client.get_version\", \"id\": null}";

    public StratumJsonMethodGetVersion(JsonNode i_json_node) throws MinyaException {
        super(i_json_node);
        String s = i_json_node.get("method").asText();
        if (s.compareTo("client.get_version") != 0) {
            throw new MinyaException();
        }
        return;
    }
}