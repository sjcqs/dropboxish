package com.dropboxish.client;

import com.dropboxish.client.command.*;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.Stoppable;
import com.dropboxish.client.utils.RequestManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/21/17.
 * The client
 */
public class Client implements Runnable, Stoppable {
    private static final Logger logger = Logger.getLogger("client");
    private static Client instance = null;

    private final CommandParser parser;
    private final BufferedReader reader;
    private boolean stopped = false;
    private String serverUrl;
    private int serverPort;
    private RequestManager requestManager;


    public static Client getInstance(BufferedReader reader, String url, int port) {
        if (instance == null){
            instance = new Client(reader, url, port);
        }
        return instance;
    }

    private Client(BufferedReader reader, String url, int port){
        this.reader = reader;
        this.serverUrl = url;
        this.serverPort = port;
        Command[] commands = new Command[]{
                new UploadFileCommand(this),
                new DownloadFileCommand(this),
                new ListFilesCommand(this),
                new SearchFilesCommand(this),
                new RemoveFileCommand(this),
                new ExitCommand(this)
        };
        parser = new CommandParser(reader, commands, new HelpCommand(commands));
    }

    @Override
    public void run() {
        ConsoleUtils.printAppInfo();
        try {
            requestManager = RequestManager.getInstance(this, serverUrl, serverPort);
        } catch (Exception e) {
            ConsoleUtils.printError("Error creating a socket.");
            return;
        }
        requestManager.connect(new RequestManager.ConnectionListener(){
            @Override
            public void connected() {
                startApp();
            }

            @Override
            public void stop() {
                Client.this.stop();
            }
        });
    }

    private void startApp() {
        Command command;
        ConsoleUtils.printAppInfo();
        while (!stopped) {
            try {
                ConsoleUtils.printPrompt(">");
                command = parser.readCommand();
                if (command != null) {
                    command.run();
                } else {
                    logger.info("Quit");
                    Client.this.stop();
                }
            } catch (CommandIllegalArgumentException e){
                ConsoleUtils.printError(e.getLines());
            } catch (IOException e) {
                logger.warning("Error: " + e.getMessage());
                Client.this.stop();
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
