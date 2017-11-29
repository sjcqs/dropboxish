package com.dropboxish.client.utils;

import com.dropboxish.client.command.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Handle the authentication of the user
 */
public class LoginManager implements Stoppable {

    private static final String DEFAULT_URL = "http://localhost";
    private static final int DEFAULT_PORT = 2222;
    private final HttpClient client;
    private String serverUrl = DEFAULT_URL;
    private int serverPort = DEFAULT_PORT;

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
    private boolean over = false;
    private String token = null;
    private Logger logger = Logger.getLogger("client");
    private BufferedReader reader;

    private LoginManager(BufferedReader reader, String url, int port) throws Exception {
        this.reader = reader;
        parser = new CommandParser(reader, commands, new HelpCommand(commands));
        SslContextFactory sslContextFactory = new SslContextFactory();
        client = new HttpClient(sslContextFactory);
        serverUrl = url;
        serverPort = port;
        client.start();
    }

    public static LoginManager getInstance(BufferedReader reader, String url, int port) throws Exception {
        if (instance == null){
            instance = new LoginManager(reader, url, port);
        }
        return instance;
    }

    public void connect(ConnectionListener connectionListener){
        Command command;
        ConsoleUtils.printShifted("");
        ConsoleUtils.printTitle("Login into the application.");
        ConsoleUtils.printShifted("You need to login before using the application.");
        while (!over && !isConnected()) {
            try {
                ConsoleUtils.printPrompt(">");
                command = parser.readCommand();
                if (command != null) {
                    command.run();
                } else {
                    stop();
                }

                if (token != null){
                    over = true;
                }
            } catch (IOException e) {
                stop();
                logger.warning("Error: " + e.getMessage());
            }
        }

        if (isConnected()){
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
        over = true;
        try {
            client.stop();
        } catch (Exception ignored) {
            // Don't care we are exiting
        }
    }

    private boolean isConnected(){
        return token != null;
    }

    public BufferedReader getReader() {
        return reader;
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

    private void setHeader(Request request) {
        request.header("Bearer:", token);
    }

    public ContentResponse sendGETRequest(String path, boolean useToken){
        try {
            return client.GET(URI.create(serverUrl + ":" + serverPort+ path));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ConsoleUtils.printDebug(e.getMessage());
            // TODO fix this shitty message
        }
        return null;
    }

    public ContentResponse sendPOSTRequest(String path, Map<String,String> params, boolean useToken){
        try {
            Request request = client.POST(URI.create(serverUrl + ":" + serverPort + path));
            //request.path(path);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");

            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.param(entry.getKey(),entry.getValue());
            }
            if (useToken){
                setHeader(request);
            }
            return request.send();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ConsoleUtils.printDebug(e.getMessage());
            // TODO fix this shitty message
        }
        return null;
    }
}
