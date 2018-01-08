package com.dropboxish.pool;

import com.dropboxish.pool.grpc.PoolServer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * Storage Pool application
 */
public class App {
    private final static Logger logger = Logger.getLogger("Pool");
    private final static int PORT = 8060;
    public static void main(String[] args) {
        PoolServer server = new PoolServer(PORT);
        try {
            server.start();
            server.blockUntilShutdown();
        } catch (IOException e) {
            logger.severe(e.getMessage());
            System.exit(-1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
