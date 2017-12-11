package com.dropboxish.client.command;

/**
 * Created by satyan on 11/21/17.
 * Used when {@link Command} was found.
 */
public class CommandNotFound extends Command {
    public CommandNotFound() {
        super("not found");
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        throw new CommandIllegalArgumentException("Command not found");
    }

    @Override
    public void help() {}
}
