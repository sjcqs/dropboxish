package com.dropboxish.controller.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by satyan on 1/10/18.
 *
 */
public class FileInfo implements Serializable{
    private final String checksum;
    private final long length;
    private final String name;

    private final List<ChunkInfo> chunks = new ArrayList<>();

    public FileInfo(String name, String checksum, long length) {
        this.checksum = checksum;
        this.length = length;
        this.name = name;
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

    public String getName() {
        return name;
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
        return String.format("%s. checksum: %s, length: %d\n%s", name, checksum, length, str);
    }
}
