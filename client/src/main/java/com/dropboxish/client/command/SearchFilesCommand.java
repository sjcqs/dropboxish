package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/21/17.
 * Search file whose name contains PATTERN
 */
public class SearchFilesCommand extends ListFilesCommand {
    private final static String PATH = "/file/list";
    public SearchFilesCommand(User user) {
        super(user, "search");
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.isEmpty()){
            throw new CommandIllegalArgumentException("Missing the search pattern.");
        }
        Map<String, String> params = new HashMap<>();
        String query = "";
        for (String arg : args) {
            query += arg + " ";
        }
        params.put("query", query.trim());
        String response = sendRequest(params);
        printFiles(response);
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("search PATTERN [PATTERN2...]", "Search file whose name contains PATTERN");
    }
}
