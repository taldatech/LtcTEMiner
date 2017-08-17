package com.example.android.ltcteminer.StratumMiner.Stratum;

import com.example.android.ltcteminer.StratumMiner.MinyaException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Tal on 09/08/2017.
 */

public class StratumJsonMethod extends StratumJson
{
    public final Long id;

    public StratumJsonMethod(JsonNode i_json_node) throws MinyaException {
        if (i_json_node.has("id")){
            this.id = i_json_node.get("id").isNull()?null:i_json_node.get("id").asLong();
        } else {
            this.id = null;
        }
        if(!i_json_node.has("method")){
            throw new MinyaException();
        }
    }
}