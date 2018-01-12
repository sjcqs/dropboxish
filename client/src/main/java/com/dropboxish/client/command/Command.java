package com.dropboxish.client.command;

import com.dropboxish.client.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by satyan on 11/22/17.
 * Command executed when the user is authenticated
 */
public abstract class Command implements Runnable{
    /**
     * {@link User} used facultative
     */
    private User user;
    /**
     * the name of this {@link Command}, write by the user to be run.
     */
    private final String name;

    private final boolean thread;

    public Command(String name) {
        this(name, false);
    }

    protected Command(String name, User user) {
        this(name, user, false);
    }

    public Command(String name, boolean thread) {
        this.name = name.trim();
        this.thread = thread;
    }

    protected Command(String name, User user, boolean thread) {
        this(name, thread);
        this.user = user;

    }

    protected User getUser() {
        return user;
    }

    protected List<String> args = new LinkedList<>();

    public boolean isThread() {
        return thread;
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

    public void check() {}
}
