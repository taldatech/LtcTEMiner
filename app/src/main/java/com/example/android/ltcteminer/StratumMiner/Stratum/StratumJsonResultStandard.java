package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJsonResultStandard extends StratumJsonResult
{
    public final static String TEST_PATT = "{\"error\": null, \"jsonrpc\": \"2.0\", \"id\": 2, \"result\": true}";
    public final boolean result;
    public StratumJsonResultStandard(JsonNode i_json_node) throws MinyaException
    {
        super(i_json_node);
        this.result=i_json_node.get("result").asBoolean();
        return;
    }
}