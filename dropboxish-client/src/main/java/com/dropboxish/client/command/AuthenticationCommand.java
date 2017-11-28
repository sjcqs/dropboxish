package com.dropboxish.client.command;

import com.dropboxish.client.utils.LoginManager;

/**
 * Created by satyan on 11/22/17.
 */
public abstract class AuthenticationCommand extends Command{
    private final LoginManager manager;

    protected AuthenticationCommand(LoginManager loginManager, String name) {
        super(name);
        this.manager = loginManager;
    }

    protected LoginManager getManager() {
        return manager;
    }
}
