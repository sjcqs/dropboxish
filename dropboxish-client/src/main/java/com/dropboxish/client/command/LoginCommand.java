package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.LoginManager;

/**
 * Created by satyan on 11/22/17.
 * usage: login USERNAME
 */
public class LoginCommand extends AuthenticationCommand {
    public LoginCommand(LoginManager loginManager){
        super(loginManager, "login");
    }

    @Override
    public void run() throws IllegalArgumentException {

    }

    @Override
    public void help() {
        ConsoleUtils.printLines("login USERNAME","Log to the application");
    }
}
