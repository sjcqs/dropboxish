package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/30/17.
 * Remove files from the cloud
 */
public class RemoveFileCommand extends RestCommand {
    private final static String PATH = "/file/remove";

    public RemoveFileCommand(Client client) {
        super("remove", client, PATH, HttpMethod.DELETE, true);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.size() == 0){
            throw new CommandIllegalArgumentException("You must specify the files to remove",
                    "usage: remove FILENAME [FILENAME2 FILENAME3...]");
        }
        String query = String.join(" ", args);

        Map<String, String> params = new HashMap<>();
        params.put("query", query);

        ContentResponse response = sendRequest(params);
        // TODO TREAT RESULT
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("remove FILENAME [FILENAME2 FILENAME3...]", "Remove a file from the cloud");
    }
}
