package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJsonMethodSetDifficulty extends StratumJsonMethod
{
    public final static String TEST_PATT = "{\"params\": [533.210506917676], \"jsonrpc\": \"2.0\", \"method\": \"mining.set_difficulty\", \"id\": 44016281}";

    // public parameter
    public double difficulty;

    public StratumJsonMethodSetDifficulty(JsonNode i_json_node) throws MinyaException {
        super(i_json_node);
        String s = i_json_node.get("method").asText();
        if (s.compareTo("mining.set_difficulty") != 0) {
            throw new MinyaException();
        }
        this.difficulty = i_json_node.get("params").get(0).asDouble();
    }
}
