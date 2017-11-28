package com.dropboxish.client.utils;

import com.dropboxish.client.command.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Handle the authentication of the user
 */
public class LoginManager implements Stoppable {
    /**
     * Singleton for {@link LoginManager}
     */
    private static LoginManager instance;

    /**
     * The list of executable {@link Command} for the {@link LoginManager}
     */
    private final Command[] commands = new Command[]{
            new LoginCommand(this),
            new RegisterCommand(this),
            new ExitCommand(this)
    };
    /**
     * A {@link CommandParser} to parse input commands
     */
    private final CommandParser parser;
    private boolean stopped = false;
    private String token = null;
    private Logger logger = Logger.getLogger("client");

    private LoginManager(){
        parser = new CommandParser(new InputStreamReader(System.in), commands, new HelpCommand(commands));
    }

    public static LoginManager getInstance() {
        if (instance == null){
            instance = new LoginManager();
        }
        return instance;
    }

    public void connect(ConnectionListener connectionListener){
        Command command;
        ConsoleUtils.printLines("");
        ConsoleUtils.printTitle("Login into the application.");
        ConsoleUtils.printLines("You need to login before using the application.");
        while (!stopped && !isConnected()) {
            try {
                System.out.print("> ");
                command = parser.readCommand();
                if (command != null) {
                    command.run();
                } else {
                    stop();
                }
            } catch (IOException e) {
                stop();
                logger.warning("Error: " + e.getMessage());
            }
        }

        if (!stopped && isConnected()){
            connectionListener.connected();
        } else {
            connectionListener.stop();
        }
    }

    public void disconnect(){
        token = null;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    private boolean isConnected(){
        return token != null;
    }

    public interface ConnectionListener {
        void connected();
        void stop();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
