/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.mqtt;

import com.zipato.model.attribute.AttributeValueEvent;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by dbudor on 02/06/2014.
 */
public class AttributeCodec {
    private static final int TYPE_INT = 1;
    private static final int TYPE_FLOAT = 2;
    private static final int TYPE_BYTE = 3;
    private static final int TYPE_STRING = 4;
    private static final int TYPE_JSON = 5;
    private static final int TYPE_DOUBLE = 6;
    private static final int TYPE_LONG = 7;
    private static final int HAVE_VALUE = 16;
    private static final int HAVE_PENDING = 32;

    private static final int KIND_SHIFT = 6;
    private static final int KIND_EVENT = 64;
    private static final int KIND_ACK = 128;
    private static final int KIND_KEEPALIVE = 0;

    public static AttributeValueEvent decode(ByteBuffer bb) {
        AttributeValueEvent rest = new AttributeValueEvent();
        long hi = bb.getLong();
        long lo = bb.getLong();
        rest.setUuid(new UUID(hi, lo));
        int flags = bb.get() & 0xff;
        //        if ((flags & HAVE_VALUE) != 0) {
        //            rest.setTimestamp(new Date(bb.getLong()));
        //            rest.setProgress(getValue(bb, flags));
        //        }
        //        if ((flags & HAVE_PENDING) != 0) {
        //            rest.setPendingTimestamp(new Date(bb.getLong()));
        //            rest.setPendingValue(getValue(bb, flags));
        //        }
        return rest;
    }

    private static Object getValue(ByteBuffer bb, int flags) {
        switch (flags & 0xf) {
            case TYPE_INT:
                return bb.getInt();
            case TYPE_BYTE:
                return bb.get() & 0xff;
            case TYPE_FLOAT:
                return bb.getFloat();
            case TYPE_DOUBLE:
                return bb.getDouble();
            case TYPE_LONG:
                return bb.getLong();
            case TYPE_STRING:
            case TYPE_JSON:
                byte[] buf = getBytes(bb);
                return new String(buf);
        }
        return null;
    }

    private static byte[] getBytes(ByteBuffer bb) {
        int len = bb.get() & 0xff;
        byte[] buf = new byte[len];
        bb.get(buf);
        return buf;
    }
}
