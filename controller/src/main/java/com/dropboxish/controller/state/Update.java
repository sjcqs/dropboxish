package com.dropboxish.controller.state;

import java.io.Serializable;

/**
 * Created by satyan on 1/8/18.
 *
 */
public class Update implements Serializable{
    private final Type type;
    private final FileInfo info;

    public enum Type {
        ADD_FILE,
        REMOVE_FILE
    }

    public Update(Type type, FileInfo info){
        this.type = type;
        this.info = info;
    }

    public FileInfo getInfo() {
        return info;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.name() + ": " + info;
    }
}
