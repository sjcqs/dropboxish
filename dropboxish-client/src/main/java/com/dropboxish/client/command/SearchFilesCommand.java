package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 * Search file whose name contains PATTERN
 */
public class SearchFilesCommand extends ClientCommand {
    public SearchFilesCommand(Client client) {
        super( client,"search");
    }

    @Override
    public void run() throws IllegalArgumentException {
        //TODO implementation
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("search PATTERN", "Search file whose name contains PATTERN");
    }
}
