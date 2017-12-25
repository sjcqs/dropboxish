package com.dropboxish.controller;

import com.dropboxish.controller.grpc.ControllerServer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * Controller app
 */
public class App {
    private static final int PORT = 8091;
    private static final Logger logger = Logger.getLogger("Controller");

    public static void main(String[] args) {
        /* // JGROUP STUFF
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("sequencer.xml");
        channel = new JChannel(stream).setReceiver(this);*/
        ControllerServer server = new ControllerServer(PORT);
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
