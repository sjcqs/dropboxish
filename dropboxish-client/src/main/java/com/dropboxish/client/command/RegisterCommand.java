package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.LoginManager;

/**
 * Created by satyan on 11/22/17.
 * This command allows the user to register and connect to the server
 * usage: register USERNAME
 */
public class RegisterCommand extends AuthenticationCommand {

    public RegisterCommand(LoginManager manager){
        super(manager,"register");
    }

    @Override
    public void run() throws IllegalArgumentException {

    }

    @Override
    public void help() {
        ConsoleUtils.printLines("register USERNAME","Register a new account to the application");
    }
}
