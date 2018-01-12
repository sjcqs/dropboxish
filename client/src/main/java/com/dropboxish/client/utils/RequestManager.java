package com.dropboxish.client.utils;

import com.dropboxish.client.User;
import com.dropboxish.client.command.*;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Handle the authentication of the user
 */
public class RequestManager implements Stoppable {
    private static final Logger logger = Logger.getLogger("httpClient");
    private static final String DEFAULT_URL = "http://localhost";
    private static final int DEFAULT_PORT = 2222;

    private Client client;
    private String url = DEFAULT_URL;

    private int port = DEFAULT_PORT;

    private WebTarget base;

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

    private RequestManager(User user, String url, int port){
        final Command[] commands = new Command[]{
                new LoginCommand(user),
                new RegisterCommand(user),
                new ExitCommand(this)
        };

        parser = new CommandParser(user.getReader(), commands, new HelpCommand(commands));

        initClient();
        base = client.target("http://"+url+":"+port);

        this.url = url;
        this.port = port;
    }

    private void initClient() {
        this.client =  ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();
    }

    public static RequestManager getInstance(User user, String url, int port) throws Exception {
        if (instance == null){
            instance = new RequestManager(user, url, port);
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
                ConsoleUtils.printPrompt();
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
        stopped = false;
        initClient();
        base = client.target("http://"+ url +":"+ port);
    }

    @Override
    public void stop() {
        stopped = true;
        try {
            client.close();
        } catch (Exception ignored) {
            // Don't care we are exiting
        }
    }

    public boolean isConnected(){
        return token != null;
    }

    public WebTarget getBase() {
        return base;
    }

    public interface ConnectionListener {
        void connected();
        void stop();
    }

    public void setToken(String token) {
        this.token = token;

        Feature feature = OAuth2ClientSupport.feature(token);
        client.register(feature);
        base = client.target("http://"+ url +":"+ port);
    }

    private WebTarget setParams(WebTarget base, Map<String, String> params){
        for (Map.Entry<String, String> entry : params.entrySet()) {
            base = base.queryParam(entry.getKey(), entry.getValue());
        }
        return base;
    }

    public Response sendRequest(String path, Map<String, String> params, String method){
        WebTarget target = base.path(path);

        if (params != null) {
            target = setParams(target, params);
        }
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        Response response = null;

        switch (method) {
            case HttpMethod.GET:
                response = builder.get();
                break;
            case HttpMethod.POST:
                response = builder.accept(MediaType.APPLICATION_JSON).post(null);
                break;
            case HttpMethod.DELETE:
                response = builder.delete();
                break;
        }
        return response;
    }
}
