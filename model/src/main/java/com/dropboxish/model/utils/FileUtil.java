package com.dropboxish.model.utils;

import com.dropboxish.model.FileInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/5/17.
 * A file
 */
public class FileUtil {
    private static final String METHOD = "SHA-256";
    private static final Logger logger = Logger.getLogger("FileUtil");
    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final int CHUNK_SIZE = 5_000_000;

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String checksum(Path file) {
        try (InputStream in = new FileInputStream(file.toFile())) {
            MessageDigest fileDigest = MessageDigest.getInstance(METHOD);
            byte[] data = new byte[4096];
            int read;
            while ((read = in.read(data)) > 0) {
                fileDigest.update(data, 0, read);
            }
            return bytesToHex(fileDigest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String checksum(ByteBuffer buffer) {
        try {
            MessageDigest fileDigest = MessageDigest.getInstance(METHOD);
            fileDigest.update(buffer);
            return bytesToHex(fileDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean check(Path file, String checksum) {
        String sum = checksum(file);
        return sum != null && sum.equals(checksum);
    }

    public static boolean check(ByteBuffer buffer, String checksum) {
        String sum = checksum(buffer);
        return sum != null && sum.equals(checksum);
    }

    public static String serialize(List<FileInfo> files) {
        return GsonUtil.GSON.toJson(files);
    }

    public static List<FileInfo> deserializeList(String json){
        return GsonUtil.GSON.fromJson(json, GsonUtil.LIST_FILE_INFO_TYPE);
    }

    public static Path[] chunks(Path file) throws IOException {
        int count =  (int) Math.ceil((double)Files.size(file) / (double)CHUNK_SIZE);
        String filename = file.toString();
        Path chunks[] = new Path[count];
        int i = 0;

        try(InputStream in = Files.newInputStream(file, StandardOpenOption.DELETE_ON_CLOSE)){
            int read;
            byte buff[] = new byte[CHUNK_SIZE];
            while ((read = in.read(buff)) > 0) {
                logger.info("Create: " + filename + String.format(".part%03d",i));
                Path chunk = Paths.get(filename + String.format(".part%03d",i));
                try(OutputStream stream = Files.newOutputStream(chunk, StandardOpenOption.CREATE)){
                    stream.write(buff, 0, read);
                }
                chunks[i] = chunk;
                i++;
            }
        }
        return chunks;
    }
}
