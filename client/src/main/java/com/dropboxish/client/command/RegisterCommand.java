package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/22/17.
 * This command allows the user to register and connect to the server
 * usage: register USERNAME
 */
public class RegisterCommand extends AuthenticationCommand {
    public final static String PATH = "/user/register";

    public RegisterCommand(User user){
        super("register", user, PATH);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.size() != 1){
            throw new CommandIllegalArgumentException(
                    "Wrong usage of register command",
                    "usage: register USERNAME");
        }
        String token = sendAuthenticationRequest();
        if (token != null){
            ConsoleUtils.print("You are registered.","Use login command to log in.");
        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("register USERNAME","Register a new account to the application");
    }
}
