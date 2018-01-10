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
        int port = PORT;
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        PoolServer server = new PoolServer(port);
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
