package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/22/17.
 * usage: login USERNAME
 */
public class LoginCommand extends AuthenticationCommand {
    private static final String PATH = "/user/login";
    public LoginCommand(User user){
        super("login", user, PATH);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.size() != 1){
            throw new CommandIllegalArgumentException(
                    "Wrong usage of login command",
                    "usage: login USERNAME");
        }
        String token = sendAuthenticationRequest();
        String username = args.get(0);
        if (token != null){
            getUser().getRequestManager().setToken(token);
            ConsoleUtils.printTitle("You are now logged in.");
        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("login USERNAME","Log to the application");
    }
}
