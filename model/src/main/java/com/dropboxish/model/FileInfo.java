package com.dropboxish.model;

import com.dropboxish.model.interfaces.JsonSerializable;

/**
 * Created by satyan on 12/6/17.
 */
public class FileInfo implements JsonSerializable<FileInfo> {
    private final String filename;
    private final long size;
    private final String checksum;
    private final String owner;

    public FileInfo(String filename, String checksum, long size, String owner) {
        this.filename = filename;
        this.size = size;
        this.checksum = checksum;
        this.owner = owner;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toJson() {
        return null;
    }

    @Override
    public FileInfo fromJson(String json) {
        return null;
    }
}
