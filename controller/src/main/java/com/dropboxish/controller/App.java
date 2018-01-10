package com.dropboxish.controller;

import com.dropboxish.controller.grpc.ControllerServer;
import com.dropboxish.pool.grpc.PoolClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * Controller app
 */
public class App {
    private static final int PORT = 8090;
    private static final Logger logger = Logger.getLogger("Controller");

    public static void main(String[] args) {
        int port = PORT;
        List<PoolClient> pools = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg){
                case "-port":
                    i++;
                    port = Integer.parseInt(args[i]);
                    break;
                case "-pools":
                    i++;
                    int count = Integer.parseInt(args[i]);
                    while (count > 0){
                        count --;
                        i++;
                        String addr[] = args[i].split(":");
                        String host = addr[0];
                        int p = Integer.valueOf(addr[1]);

                        PoolClient client = new PoolClient(host, p);
                        pools.add(client);
                    }
                    break;
                default:
                    break;
            }
        }
        if (pools.isEmpty()){
            System.err.println("You have to specify the addresses of the pools.");
            System.exit(-1);
        }

        Controller controller = new Controller();
        ControllerServer server = new ControllerServer(controller, port, pools);
        try {
            server.start();
            controller.start();
            server.blockUntilShutdown();
        } catch (IOException e) {
            logger.severe(e.getMessage());
            System.exit(-1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
