package com.dropboxish.controller.state;

import com.dropboxish.model.Host;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by satyan on 1/10/18.
 *
 */
public class ChunkInfo implements Serializable{
    private final String checksum;
    private final long length;
    private final int index;

    private final Map<String, Host> blockMap = new HashMap<>();
    private final List<BlockInfo> blocks = new ArrayList<>();

    public ChunkInfo(int index, String checksum, long length) {
        this.checksum = checksum;
        this.length = length;
        this.index = index;
    }

    /**
     * Get the pool containing a certain block.
     * @param checksum block's checksum
     * @return host containing the block
     */
    public Host getHost(String checksum){
        return blockMap.get(checksum);
    }

    public void putBlock(BlockInfo block, Host host){
        blockMap.put(block.getChecksum(), host);
        blocks.add(block);
    }

    public List<BlockInfo> getBlocks(){
        return blocks;
    }

    public String getChecksum() {
        return checksum;
    }

    public long getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        String str = "";
        for (BlockInfo block : blocks) {
            str += "\t\t" + block.toString() + " at " + blockMap.get(block.getChecksum()) + "\n";
        }
        return String.format("%d. checksum:  %s, length: %d\n%s", index, checksum, length, str);
    }
}
