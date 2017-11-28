package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 */
public class CommandNotFound extends Command {
    public CommandNotFound() {
        super("not found");
    }

    @Override
    public void run() throws IllegalArgumentException {
        ConsoleUtils.printError("Command not found");
    }

    @Override
    public void help() {}
}
