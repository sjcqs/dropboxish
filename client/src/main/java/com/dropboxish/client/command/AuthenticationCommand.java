package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.RequestManager;

import javax.ws.rs.HttpMethod;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/22/17.
 *
 */
abstract class AuthenticationCommand extends RestCommand{
    private static final int USERNAME_MAX_LENGTH = 32;
    private static final int USERNAME_MIN_LENGTH = 4;
    private final RequestManager manager;

    protected AuthenticationCommand(String name, User user, String path) {
        super(name, user, path, HttpMethod.POST);
        this.manager = user.getRequestManager();
    }

    protected String sendAuthenticationRequest(){
        String username = args.get(0);
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH){
            throw new CommandIllegalArgumentException(
                    String.format("Username length should be between %d and %d characters.",
                            USERNAME_MIN_LENGTH,
                            USERNAME_MAX_LENGTH)
            );
        }
        String password;
        try{
            BufferedReader reader = getUser().getReader();
            ConsoleUtils.printPrompt("password:");
            String line = reader.readLine();
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
        return sendRequest(params);
    }

    protected RequestManager getManager() {
        return manager;
    }
}
