package com.dropboxish.client.command;

import com.dropboxish.client.Client;

/**
 * Created by satyan on 11/22/17.
 * Command executed when the client is authenticated
 */
public abstract class ClientCommand extends Command{

    private final Client client;

    protected ClientCommand(Client client, String name) {
        super(name);
        this.client = client;
    }

    protected Client getClient() {
        return client;
    }
}
