/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

/**
 * Created by murielK on 8/28/2014.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SecurityUpdateItem {

    public String eventType;
    public int code;
    public String transactionID;
    public Map<String, Object> datajson;
    public String message;
    public String type;
    public String clientSessionId;
    public String nonce;
    public String salt;
    public String secureSessionId;
    public boolean success;
}
