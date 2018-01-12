package com.dropboxish.model;

import java.io.Serializable;

/**
 * Created by satyan on 1/10/18.
 */
public class Host implements Serializable{
    private final String host;
    private final int port;

    public Host(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Host &&
                ((Host) o).getHost().equals(host)
                && ((Host) o).getPort() == port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }
}
