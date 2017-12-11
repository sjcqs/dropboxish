package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import javax.ws.rs.HttpMethod;

/**
 * Created by satyan on 11/21/17.
 * Search file whose name contains PATTERN
 */
public class SearchFilesCommand extends RestCommand {
    private final static String PATH = "/file/list";
    public SearchFilesCommand(User user) {
        super("search", user, PATH, HttpMethod.GET);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        String response = sendRequest();
        if (response != null){

        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("search PATTERN", "Search file whose name contains PATTERN");
    }
}
