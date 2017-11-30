package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.RequestManager;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/22/17.
 *
 */
public abstract class AuthenticationCommand extends RestCommand{
    private final RequestManager manager;

    protected AuthenticationCommand(String name, Client client, String path) {
        super(name, client, path, HttpMethod.POST, false);
        this.manager = client.getRequestManager();
    }

    protected String sendAuthenticationRequest(){
        String username = args.get(0);
        String password;
        try{
            BufferedReader reader = getClient().getReader();
            ConsoleUtils.printPrompt("password:");
            String line = reader.readLine();
            System.out.println(line);
            if (line == null){
                getManager().stop();
                return null;
            }
            password = line;
        } catch (IOException e) {
            throw new CommandIllegalArgumentException("Unexpected error when reading the password.");
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password",password);
        ContentResponse response = sendRequest(params);
        return response.getContentAsString();
    }

    protected RequestManager getManager() {
        return manager;
    }
}
