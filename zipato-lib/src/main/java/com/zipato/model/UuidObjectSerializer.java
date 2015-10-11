/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by murielK on 11/12/2014.
 */
public class UuidObjectSerializer extends JsonSerializer<UUIDObject> {

    @Override
    public void serialize(UUIDObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeFieldName("uuid");
        jgen.writeString(value.getUuid().toString());
        jgen.writeEndObject();
    }
}
