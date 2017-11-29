package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.LoginManager;

/**
 * Created by satyan on 11/22/17.
 * usage: login USERNAME
 */
public class LoginCommand extends AuthenticationCommand {
    private final String URL = "/user/login";
    public LoginCommand(LoginManager loginManager){
        super(loginManager, "login");
    }

    @Override
    public void run() throws IllegalArgumentException {
        if (args.size() != 1){
            ConsoleUtils.printError(
                    "Wrong usage of login command",
                    "usage: login USERNAME");
            return;
        }
        String token = sendAuthenticationRequest(URL);
        if (token != null){
            getManager().setToken(token);
            ConsoleUtils.printTitle("You are now logged in.");
        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("login USERNAME","Log to the application");
    }
}
