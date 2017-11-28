package com.dropboxish.client.command;

import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.Stoppable;

import java.util.logging.Logger;

/**
 * Created by satyan on 11/21/17.
 */
public class ExitCommand extends Command {
    private static final Logger logger = Logger.getLogger("client");
    private final Stoppable stoppable;

    public ExitCommand(Stoppable stoppable) {
        super("exit");
        this.stoppable = stoppable;
    }

    @Override
    public void run() throws IllegalArgumentException{
        stoppable.stop();
    }

    @Override
    public void help() {
        ConsoleUtils.printLines("exit","Exit the application");
    }
}
