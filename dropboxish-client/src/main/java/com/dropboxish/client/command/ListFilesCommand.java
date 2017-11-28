package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 * List available files (both locally and in the cloud)
 */
public class ListFilesCommand extends ClientCommand {
    public ListFilesCommand(Client client) {
        super(client,"list");
    }

    @Override
    public void run() throws IllegalArgumentException {
        //TODO implementation
    }

    @Override
    public void help() {
        ConsoleUtils.printLines("list","List available files (both locally and in the cloud)");
    }
}
