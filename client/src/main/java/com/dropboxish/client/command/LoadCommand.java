package com.dropboxish.client.command;

import com.dropboxish.client.User;

/**
 * Created by satyan on 11/30/17.
 * A {@link Command} that uses the REST/API
 */
abstract class LoadCommand extends RestCommand {

    protected LoadCommand(String name, User user, String path, String method) {
        super(name, user, path, method, true);
    }

}
