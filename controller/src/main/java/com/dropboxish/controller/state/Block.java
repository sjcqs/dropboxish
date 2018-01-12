package com.dropboxish.controller.state;

import com.dropboxish.model.utils.FileUtil;

import java.nio.ByteBuffer;

/**
 * Created by satyan on 1/9/18.
 *
 */
public class Block {
    private  final ByteBuffer buffer;
    private final BlockInfo info;

    public Block(byte bytes[], BlockInfo.Type type, int index, int length) {
        buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        info = new BlockInfo(FileUtil.checksum(buffer), type, index, bytes.length, length);
        buffer.rewind();
    }

    public Block(BlockInfo info, ByteBuffer buffer) {
        this.info = info;
        this.buffer = buffer;
    }

    public Block(BlockInfo info, byte[] dx) {
        this.info = info;
        this.buffer = ByteBuffer.wrap(dx);
    }

    public ByteBuffer getBuffer() {
        buffer.rewind();
        return buffer;
    }

    public String getChecksum() {
        return info.getChecksum();
    }

    public BlockInfo.Type getType() {
        return info.getType();
    }

    public int getLength() {
        return info.getLength();
    }

    public int getIndex() {
        return info.getIndex();
    }

    public BlockInfo getInfo() {
        return info;
    }

    public int getRealLength() {
        return info.getRealLength();
    }
}
