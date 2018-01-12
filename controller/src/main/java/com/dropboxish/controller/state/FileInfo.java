package com.dropboxish.controller.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by satyan on 1/10/18.
 *
 */
public class FileInfo implements Serializable{
    private final String owner;
    private final String filename;
    private final String checksum;

    private final long length;
    private final List<ChunkInfo> chunks = new ArrayList<>();

    public FileInfo(String owner, String filename, String checksum, long length) {
        this.owner = owner;
        this.checksum = checksum;
        this.length = length;
        this.filename = filename;
    }

    public void addChunk(ChunkInfo chunk){
        chunks.add(chunk);
    }

    public String getChecksum() {
        return checksum;
    }

    public long getLength() {
        return length;
    }

    public String getFilename() {
        return filename;
    }

    public String getOwner() {
        return owner;
    }

    public List<ChunkInfo> getChunks() {
        return chunks;
    }

    @Override
    public String toString() {
        String str = "";
        for (ChunkInfo chunk : chunks) {
            str += "\t" + chunk + "\n";
        }
        return String.format("%s/%s. checksum: %s, length: %d\n%s", owner, filename, checksum, length, str);
    }
}
