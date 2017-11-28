package com.dropboxish.client;

import com.dropboxish.client.command.*;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.Stoppable;
import com.dropboxish.client.utils.LoginManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/21/17.
 * The client
 */
public class Client implements Runnable, Stoppable {
    private static final Logger logger = Logger.getLogger("client");
    private static Client instance = null;

    private final CommandParser parser;
    private boolean stopped = false;


    public static Client getInstance() {
        if (instance == null){
            instance = new Client();
        }
        return instance;
    }

    private Client(){
        Command[] commands = new Command[]{
                new UploadFileCommand(this),
                new DownloadFileCommand(this),
                new ListFilesCommand(this),
                new SearchFilesCommand(this),
                new ExitCommand(this)
        };
        parser = new CommandParser(new InputStreamReader(System.in), commands, new HelpCommand(commands));

    }

    @Override
    public void run() {
        stopped = false;
        ConsoleUtils.printAppInfo();
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.connect(new LoginManager.ConnectionListener(){
            @Override
            public void connected() {
                Command command;
                while (!stopped) {
                    try {
                        System.out.print("> ");
                        command = parser.readCommand();
                        if (command != null) {
                            command.run();
                        } else {
                            stop();
                        }
                    } catch (IOException e) {
                        logger.warning("Error: " + e.getMessage());
                        stop();
                    }
                }
            }

            @Override
            public void stop() {
                Client.this.stop();
            }
        });
    }

    @Override
    public void stop() {
        stopped = true;
        try {
            parser.close();
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }

}
