package com.dropboxish.controller.state;

import java.io.Serializable;

/**
 * Created by satyan on 1/10/18.
 */
public class BlockInfo implements Serializable{
    private final String checksum;
    private final Type type;
    private final int index;
    private final int length;
    private final int realLength;

    public BlockInfo(String checksum, Type type, int index, int length, int realLength) {
        this.checksum = checksum;
        this.type = type;
        this.index = index;
        this.length = length;
        this.realLength = realLength;
    }

    public Type getType() {
        return type;
    }

    public String getChecksum() {
        return checksum;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("%d. type: %s, checksum: %s, length: %d", index, type.name(), checksum, length);
    }

    public int getRealLength() {
        return realLength;
    }

    public enum Type{
        DATA,
        PARITY,
        RS
    }
}
