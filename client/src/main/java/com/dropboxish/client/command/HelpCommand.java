package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 */
public class HelpCommand extends Command {
    private final Command[] commands;

    public HelpCommand(Command[] commands) {
        super("help");
        this.commands = commands;
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        ConsoleUtils.printAppInfo();
        ConsoleUtils.printTitle("COMMANDS");
        for (Command command : commands) {
            command.help();
        }
    }

    @Override
    public void check() {
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("help", "Print this");
    }

}
