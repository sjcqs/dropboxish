package com.dropboxish.client.command;

import com.dropboxish.client.Client;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by satyan on 11/22/17.
 * Command executed when the client is authenticated
 */
public abstract class Command implements Runnable{
    /**
     * {@link Client} used facultative
     */
    private Client client;
    /**
     * the name of this {@link Command}, write by the user to be run.
     */
    private final String name;

    public Command(String name) {
        this.name = name.trim();
    }

    protected Command(String name, Client client) {
        this(name);
        this.client = client;
    }

    protected Client getClient() {
        return client;
    }

    protected List<String> args = new LinkedList<>();


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
        for (String arg : args) {
            arg = arg.replace("\\","");
            this.args.add(arg);
        }
    }

    /**
     * Clear the arguments
     */
    public void clearArgs() {
        args.clear();
    }

    /**
     * Run the {@link Command}
     * @throws CommandIllegalArgumentException if the arguments are invalid
     */
    @Override
    public abstract void run() throws CommandIllegalArgumentException;

    /**
     * Print the help for this {@link Command}
     */
    public abstract void help();
}
