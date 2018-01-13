package com.dropboxish.client;

import com.dropboxish.client.command.*;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.RequestManager;
import com.dropboxish.client.utils.Stoppable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/21/17.
 * The client
 */
public class User implements Runnable, Stoppable {
    private static final Logger logger = Logger.getLogger("client");
    private static final int THREAD_COUNT = 5;
    private static User instance = null;

    private final CommandParser parser;
    private final BufferedReader reader;
    private final ExecutorService service;
    private boolean stopped = false;
    private String serverUrl;
    private int serverPort;
    private RequestManager requestManager;


    public static User getInstance(BufferedReader reader, String url, int port) {
        if (instance == null){
            instance = new User(reader, url, port);
        }
        return instance;
    }

    private User(BufferedReader reader, String url, int port){
        this.service = Executors.newFixedThreadPool(THREAD_COUNT);
        this.reader = reader;
        this.serverUrl = url;
        this.serverPort = port;
        Command[] commands = new Command[]{
                new UploadFileCommand(this),
                new DownloadFileCommand(this),
                new ListFilesCommand(this),
                new SearchFilesCommand(this),
                new LSCommand(this),
                new RemoveFileCommand(this),
                new ExitCommand(this)
        };
        parser = new CommandParser(reader, commands, new HelpCommand(commands));
    }

    @Override
    public void run() {
        ConsoleUtils.printAppInfo();
        while (!stopped) {
            try {
                requestManager = RequestManager.getInstance(this, serverUrl, serverPort);
            } catch (Exception e) {
                ConsoleUtils.printError("Error creating a socket.", e.getMessage());
                return;
            }
            requestManager.connect(new RequestManager.ConnectionListener() {
                @Override
                public void connected() {
                    startApp();
                }

                @Override
                public void stop() {
                    User.this.stop();
                }
            });
        }
    }

    private void startApp() {
        Command command;
        ConsoleUtils.printAppInfo();
        while (!stopped && requestManager.isConnected()) {
            try {
                ConsoleUtils.printPrompt();
                command = parser.readCommand();
                if (command != null) {
                    if (!command.isThread()){
                        command.run();
                    } else {
                        command.check();
                        service.submit(command);
                    }
                } else {
                    logger.info("Quit");
                    User.this.stop();
                }
            } catch (CommandIllegalArgumentException e){
                ConsoleUtils.printError(e.getLines());
            } catch (IOException e) {
                logger.warning("Error: " + e.getMessage());
                User.this.stop();
            }
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public BufferedReader getReader() {
        return reader;
    }

}
