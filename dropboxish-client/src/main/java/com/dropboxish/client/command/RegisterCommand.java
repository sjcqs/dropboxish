package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.LoginManager;

/**
 * Created by satyan on 11/22/17.
 * This command allows the user to register and connect to the server
 * usage: register USERNAME
 */
public class RegisterCommand extends AuthenticationCommand {
    public final static String URL = "/user/register";

    public RegisterCommand(LoginManager manager){
        super(manager,"register");
    }

    @Override
    public void run() throws IllegalArgumentException {
        if (args.size() != 1){
            ConsoleUtils.printError(
                    "Wrong usage of register command",
                    "usage: register USERNAME");
            return;
        }
        String token = sendAuthenticationRequest(URL);
        if (token != null){
            ConsoleUtils.print("You are registered.","Use login command to log in.");
        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("register USERNAME","Register a new account to the application");
    }
}
