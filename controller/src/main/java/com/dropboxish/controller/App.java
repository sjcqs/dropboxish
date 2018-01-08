package com.dropboxish.controller;

import com.dropboxish.controller.grpc.ControllerServer;

import java.io.IOException;
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
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        Controller controller = new Controller();
        ControllerServer server = new ControllerServer(port);
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
