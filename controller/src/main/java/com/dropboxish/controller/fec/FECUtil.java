package com.dropboxish.controller.fec;

import com.dropboxish.controller.state.Block;
import com.dropboxish.controller.state.BlockInfo;
import com.dropboxish.model.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 1/9/18.
 * source:
 * - https://en.wikiversity.org/wiki/Reed%E2%80%93Solomon_codes_for_coders#RS_encoding
 * - https://www.kernel.org/pub/linux/kernel/people/hpa/raid6.pdf
 */
public class FECUtil {

    private static Logger logger = Logger.getLogger("FEC");

    public static Block[] encode(Path chunk, int count) throws IOException {
        int chunkSize = Math.toIntExact(Files.size(chunk));

        if (count < 2){
            throw new IOException("You need at least 2 storage pools");
        } else if (count < 4){
            // only parity
            return parity(chunk, chunkSize, count);
        } else if (count < GF.FIELD_SIZE + 1){
            return reedSolomon(chunk, chunkSize, count);
        } else {
            throw new IOException("Impossible to create the blocks.");
        }
    }

    private static void recover(Block[] blocks, Block missing[]){
        recover(blocks, missing, false);
    }

    public static boolean recover(Block[] blocks) {
        List<Block> errors = new ArrayList<>();

        for (Block block : blocks) {
            if (!FileUtil.check(block.getBuffer(), block.getChecksum())) {
                logger.warning("Missing: " + block.getInfo());
                errors.add(block);
            }
        }
        if (!errors.isEmpty()){
            recover(blocks, errors.toArray(new Block[0]));
            for (Block error : errors) {
                Block block = blocks[error.getIndex()];
                if (block.getType().equals(BlockInfo.Type.DATA)){
                    if (!FileUtil.check(block.getBuffer(), block.getChecksum())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void recover(Block[] blocks, Block missing[], boolean dataOnly){
        int count = blocks.length;
        int m = count < 4 ? count - 1 : count - 2;
        if (missing.length <= 2){
            if (missing.length == 1){
                Block block = missing[0];
                switch (block.getType()) {
                    case DATA:
                        recoverDataBlock(blocks, block.getIndex(), m);
                        break;
                    case PARITY:
                        if (!dataOnly){
                            parityBlock(blocks, m, m);
                        }
                        break;
                    case RS:
                        if (!dataOnly){
                            rsBlock(blocks, m + 1, m);
                        }
                        break;
                }
            } else if (missing.length == 2 && count >= 4){ // two missing recoverable for more than 4 blocks
                Block m0 = missing[0];
                Block m1 = missing[1];
                // missing data && data
                if (m0.getType().equals(BlockInfo.Type.DATA) && m1.getType().equals(BlockInfo.Type.DATA)){
                    recoverDataBlocks(blocks, m0.getIndex(), m1.getIndex(), m);
                }
                // missing parity && data
                else if (m0.getType().equals(BlockInfo.Type.DATA) && m1.getType().equals(BlockInfo.Type.PARITY)){
                    recoverDataBlockNoParity(blocks, m0.getIndex(), m + 1);
                    if (!dataOnly){
                        parityBlock(blocks, m1.getIndex(), m);
                    }
                } else if (m0.getType().equals(BlockInfo.Type.PARITY) && m1.getType().equals(BlockInfo.Type.DATA)){
                    recoverDataBlockNoParity(blocks, m1.getIndex(), m + 1);
                    if (!dataOnly) {
                        parityBlock(blocks, m0.getIndex(), m);
                    }
                }
                // missing  rs && data
                else if (m0.getType().equals(BlockInfo.Type.DATA) && m1.getType().equals(BlockInfo.Type.RS)){
                    recoverDataBlock(blocks, m0.getIndex(), m);
                    if (!dataOnly){
                        rsBlock(blocks, m1.getIndex(), m);
                    }
                } else if (m0.getType().equals(BlockInfo.Type.RS) && m1.getType().equals(BlockInfo.Type.DATA)){
                    recoverDataBlock(blocks, m1.getIndex(), m);
                    if (!dataOnly) {
                        rsBlock(blocks, m0.getIndex(), m);
                    }
                }
                // missing parity && rs
                else if (!dataOnly) {
                    if ((m0.getType().equals(BlockInfo.Type.PARITY) && m1.getType().equals(BlockInfo.Type.RS))){
                        parityBlock(blocks, m0.getIndex(), m);
                        rsBlock(blocks, m1.getIndex(), m);
                    } else if (m1.getType().equals(BlockInfo.Type.PARITY) && m0.getType().equals(BlockInfo.Type.RS)) {
                        parityBlock(blocks, m1.getIndex(), m);
                        rsBlock(blocks, m0.getIndex(), m);
                    }
                }
            }
        }
    }

    private static Block[] split(Path chunk, int size, int count) throws IOException {
        Block[] blocks = new Block[count];
        try(InputStream in = Files.newInputStream(chunk, StandardOpenOption.DELETE_ON_CLOSE)){
            int i = 0;
            byte bytes[] = new byte[size];
            int read;
            while ((read = in.read(bytes)) > 0){
                blocks[i] = new Block(bytes, BlockInfo.Type.DATA, i, read);
                i++;
            }
        }
        return blocks;
    }


    private static Block[] parity(Path chunk, int chunkSize, int count) throws IOException {

        final int p = 1;
        final int m = count - p;
        int blockSize = (int) Math.ceil((double) chunkSize / (double) m);
        Block[] blocks = split(chunk, blockSize, count);
        computeParity(blocks, m, m);
        return blocks;
    }

    private static Block[] reedSolomon(Path chunk, int chunkSize, int count) throws IOException {
        final int p = 2;
        final int m = count - p;
        int blockSize = (int) Math.ceil((double) chunkSize / (double) m);
        Block[] blocks = split(chunk, blockSize, count);
        computeParity(blocks, m, m);
        computeRsBlock(blocks, m + 1, m);
        // Inline test, Fuck JUnit ...
        for (int i = 0; i < m; i++) {
            String checksum = blocks[i].getChecksum();
            recoverDataBlockNoParity(blocks, i, m + 1);
            assert (checksum.equals(blocks[i].getChecksum()));
            for (int j = i + 1; j < m; j++) {
                String checksum1 = blocks[j].getChecksum();
                recoverDataBlocks(blocks, i, j, m);
                assert (checksum.equals(blocks[i].getChecksum()) && checksum1.equals(blocks[j].getChecksum()));
            }
        }
        return blocks;
    }

    private static void computeRsBlock(Block[] blocks, int index, int m) {
        blocks[index] = rsBlock(blocks, index, m);
    }

    private static Block rsBlock(Block[] blocks, int index, int m) {
        byte b0[] = blocks[0].getBuffer().array().clone();

        for (int i = 1; i < m; i++) {
            byte g = GF.getInstance().pow((byte) (GF.GENERATOR & 0xff), i);
            byte b1[];
            b1 = blocks[i].getBuffer().array();
            for (int i1 = 0; i1 < b0.length; i1++) {
                b0[i1] = GF.getInstance().sum(b0[i1],GF.getInstance().mul(b1[i1], g));
            }
        }
        return new Block(b0, BlockInfo.Type.RS, index, b0.length);
    }

    private static void computeParity(final Block[] blocks, int index, int m){
        blocks[index] = parityBlock(blocks, index, m);
    }

    private static Block parityBlock(final Block[] blocks, int index, int m) {
        byte b0[] = blocks[0].getBuffer().array().clone();

        for (int i = 1; i < m; i++) {
            byte b1[];
            b1 = blocks[i].getBuffer().array();

            for (int i1 = 0; i1 < b0.length; i1++) {
                b0[i1] = GF.getInstance().sum(b0[i1], b1[i1]);
            }
        }
        return new Block(b0, BlockInfo.Type.PARITY, index, b0.length);
    }

    /**
     * Recover one data block
     * @param blocks blocks
     * @param x missing/wrong data block
     * @param m index of the parity block
     */
    private static void recoverDataBlock(final Block[] blocks, int x, int m){
        byte[] dx = blocks[m].getBuffer().array().clone();
        for (int i = 0; i < m; i++) {
            if (i != x){
                byte[] b1 = blocks[i].getBuffer().array();
                for (int i1 = 0; i1 < dx.length; i1++) {
                    dx[i1] = GF.getInstance().sum(dx[i1], b1[i1]);
                }
            }
        }
        blocks[x] = new Block(blocks[x].getInfo(), dx);
    }

    /**
     * Recover one data block with no parity block
     * @param blocks blocks
     * @param x missing/wrong data block
     * @param m index of the rs block
     */
    private static void recoverDataBlockNoParity(Block[] blocks, int x, int m) {
        byte[] dx = new byte[blocks[0].getLength()];
        blocks[x] = new Block(blocks[x].getInfo(), dx);

        byte[] rs = blocks[m].getBuffer().array();
        // Reed Solomon block with missing block as {0,...,0}
        byte[] rs_ = rsBlock(blocks, m, m - 1).getBuffer().array();
        GF gf = GF.getInstance();
        byte g = gf.pow((byte) (GF.GENERATOR), blocks[x].getIndex());

        for (int i = 0; i < dx.length; i++) {
            dx[i] = gf.div( gf.sum(rs[i], rs_[i]), g);
        }

        blocks[x] = new Block(blocks[x].getInfo(), dx);
        assert FileUtil.check(blocks[x].getBuffer(), blocks[x].getChecksum());
    }

    private static byte[] calcA(int x, int y, int size){
        GF gf = GF.getInstance();
        byte g = gf.pow((byte) (GF.GENERATOR & 0xff), y - x);

        byte res[] = new byte[size];
        for (int i = 0; i < res.length; i++) {
            res[i] = gf.div(g, (byte) (g ^ 1));
        }
        return res;
    }

    private static byte[] calcB(int x, int y, int size){
        GF gf = GF.getInstance();
        byte g0 = gf.pow((byte) (GF.GENERATOR & 0xff), y - x);
        byte g1 = gf.pow((byte) (GF.GENERATOR & 0xff), - x);

        byte res[] = new byte[size];
        for (int i = 0; i < res.length; i++) {
            res[i] = gf.div(g1, (byte) (g0 ^ 1));
        }
        return res;
    }

    /**
     * Recover two data block
     * @param blocks blocks
     * @param x first missing/wrong data block
     * @param y second missing/wrong data block
     * @param m index of the parity block
     */
    private static void recoverDataBlocks(Block[] blocks, int x, int y, int m) {
        int length = blocks[0].getLength();

        byte[] dx = new byte[length];
        byte[] dy = new byte[length];
        blocks[x] = new Block(blocks[x].getInfo(), dx);
        blocks[y] = new Block(blocks[y].getInfo(), dy);

        byte[] rs_ = rsBlock(blocks, m + 1, m).getBuffer().array();
        byte[] rs = blocks[m + 1].getBuffer().array();
        byte[] p_ = parityBlock(blocks, m, m).getBuffer().array();
        byte[] p = blocks[m].getBuffer().array();

        byte[] a = calcA(x, y, length);
        byte[] b = calcB(x, y, length);

        GF gf = GF.getInstance();

        for (int i = 0; i < dx.length; i++) {
            p_[i] = gf.sum(p[i], p_[i]);
            rs_[i] = gf.sum(rs[i], rs_[i]);

            byte b0 = gf.mul(a[i], p_[i]);
            byte b1 = gf.mul(b[i], rs_[i]);

            dx[i] = gf.sum(b0, b1);
        }

        for (int i = 0; i < dy.length; i++) {
            dy[i] = gf.sum(dx[i], p_[i]);
        }

        blocks[x] = new Block(blocks[x].getInfo(), dx);
        blocks[y] = new Block(blocks[y].getInfo(), dy);
    }
}
