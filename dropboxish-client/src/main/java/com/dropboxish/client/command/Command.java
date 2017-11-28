package com.dropboxish.client.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by satyan on 11/21/17.
 * A simple command that can be executed
 */
public abstract class Command {
    protected List<String> args = null;
    /**
     * the name of this {@link Command}, write by the user to be run.
     */
    private final String name;

    protected Command(String name) {
        this.name = name.trim();
    }

    /**
     * Return the {@link Command#name} of this command
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Set the arguments
     * @param args
     */
    public void setArgs(List<String> args) {
        this.args = new ArrayList<>();
        for (String arg : args) {
            arg = arg.replace("\\","");
            this.args.add(arg);
        }
    }

    /**
     * Clear the arguments
     */
    public void clearArgs() {
        if (args != null){
            args = null;
        }
    }

    /**
     * Run the {@link Command}
     * @throws IllegalArgumentException if the arguments are invalid
     */
    public abstract void run() throws IllegalArgumentException;

    /**
     * Print the help for this {@link Command}
     */
    public abstract void help();
}
