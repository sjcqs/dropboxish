package com.dropboxish.client.utils;

import com.dropboxish.client.Client;
import com.dropboxish.client.command.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Handle the authentication of the user
 */
public class RequestManager implements Stoppable {
    private static final Logger logger = Logger.getLogger("httpClient");
    private static final String DEFAULT_URL = "http://localhost";
    private static final int DEFAULT_PORT = 2222;

    private final HttpClient httpClient;
    private String serverUrl = DEFAULT_URL;

    private int serverPort = DEFAULT_PORT;

    /**
     * Singleton for {@link RequestManager}
     */
    private static RequestManager instance;
    /**
     * A {@link CommandParser} to parse input commands
     */
    private final CommandParser parser;

    private boolean stopped = false;
    /**
     * Token use by JWT to authenticate when using the Rest/API
     */
    private String token = null;
    /**
     * A {@link BufferedReader} to read the user input
     */
    private BufferedReader reader;

    private RequestManager(Client client, String url, int port) throws Exception {
        final Command[] commands = new Command[]{
                new LoginCommand(client),
                new RegisterCommand(client),
                new ExitCommand(this)
        };

        this.reader = client.getReader();
        parser = new CommandParser(reader, commands, new HelpCommand(commands));
        SslContextFactory sslContextFactory = new SslContextFactory();
        this.httpClient = new HttpClient(sslContextFactory);
        serverUrl = url;
        serverPort = port;
        this.httpClient.start();
    }

    public static RequestManager getInstance(Client client, String url, int port) throws Exception {
        if (instance == null){
            instance = new RequestManager(client, url, port);
        }
        return instance;
    }

    public void connect(ConnectionListener connectionListener){
        Command command;
        ConsoleUtils.printShifted("");
        ConsoleUtils.printTitle("Login into the application.");
        ConsoleUtils.printShifted("You need to login before using the application.");
        while (!stopped && !isConnected()) {
            try {
                ConsoleUtils.printPrompt(">");
                command = parser.readCommand();
                if (command != null) {
                    command.run();
                } else {
                    stop();
                }

                if (token != null) {
                    stopped = true;
                }
            } catch (CommandIllegalArgumentException e){
                ConsoleUtils.printError(e.getLines());
            }catch (IOException e) {
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
        stopped = true;
        try {
            httpClient.stop();
        } catch (Exception ignored) {
            // Don't care we are exiting
        }
    }

    private boolean isConnected(){
        return token != null;
    }

    public interface ConnectionListener {
        void connected();
        void stop();
    }

    public void setToken(String token) {
        this.token = token;
    }

    private Request newRequest(){
        return httpClient.newRequest(serverUrl, serverPort);
    }

    private void setHeader(Request request) {
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
    }

    private void setParams(Request request, Map<String, String> params){
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request.param(entry.getKey(),entry.getValue());
        }
    }

    public ContentResponse sendRequest(String path, Map<String, String> params, HttpMethod method, boolean useToken){
        try {
            Request request = newRequest();

            request.path(path);
            request.method(method);

            // params
            if (params != null){
                setParams(request, params);
            }
            // Headers
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            if (useToken){
                // NOTICE could use cookies instead but won't bother
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
