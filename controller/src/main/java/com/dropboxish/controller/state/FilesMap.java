package com.dropboxish.controller.state;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 1/8/18.
 *
 */
public class FilesMap implements Serializable, Comparable<FilesMap>{
    private final Map<String, FileInfo> files = new HashMap<>();
    private Date lastUpdate = null;

    public FilesMap(){
    }

    public void add(FileInfo file){
        files.put(file.getOwner() + ":" + file.getFilename(), file);
        lastUpdate = new Date();
    }

    public FileInfo remove(String checksum) {
        lastUpdate = new Date();
        return files.remove(checksum);
    }

    public FileInfo get(String filename, String owner){
        return files.get(owner + ":" + filename);
    }

    @Override
    public String toString() {
        String str = "";
        for (FileInfo info : files.values()) {
            str += info + "\n";
        }
        return str;
    }

    @Override
    public int compareTo(FilesMap t1) {
        if (lastUpdate == null){
            return t1.lastUpdate == null ? 0 : -1;
        } else if (t1.lastUpdate == null){
            return 1;
        }
        return lastUpdate.compareTo(t1.lastUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FilesMap){
            FilesMap fp = (FilesMap) o;
            return fp.files.keySet().equals(files.keySet());
        }
        return false;
    }
}
