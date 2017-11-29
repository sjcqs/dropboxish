package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.LoginManager;
import org.eclipse.jetty.client.api.ContentResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/22/17.
 *
 */
public abstract class AuthenticationCommand extends Command{
    private final LoginManager manager;

    protected AuthenticationCommand(LoginManager loginManager, String name) {
        super(name);
        this.manager = loginManager;
    }

    protected String sendAuthenticationRequest(String url){

        String username = args.get(0);
        String password;
        try{
            BufferedReader reader = getManager().getReader();
            ConsoleUtils.printPrompt("password:");
            String line = reader.readLine();
            System.out.println(line);
            if (line == null){
                getManager().stop();
                return null;
            }
            password = line;
        } catch (IOException e) {
            ConsoleUtils.printError("Unexpected error when reading the password.");
            return null;
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password",password);
        ContentResponse response = getManager().sendPOSTRequest(url, params,false);

        if (response.getStatus() != 200) {
            ConsoleUtils.printError(response.getContentAsString());
        } else {
            return response.getContentAsString();
        }

        return null;
    }

    protected LoginManager getManager() {
        return manager;
    }
}
