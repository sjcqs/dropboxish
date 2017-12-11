package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.google.gson.Gson;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/30/17.
 * Remove files from the cloud
 */
public class RemoveFileCommand extends RestCommand {
    private final static String PATH = "/file/remove";

    public RemoveFileCommand(User user) {
        super("remove", user, PATH, HttpMethod.DELETE);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.size() == 0){
            throw new CommandIllegalArgumentException("You must specify the files to remove",
                    "usage: remove FILENAME [FILENAME2 FILENAME3...]");
        }
        String filenames = new Gson().toJson(args);

        Map<String, String> params = new HashMap<>();
        params.put("filenames", filenames);

        String response = sendRequest(params);
        ConsoleUtils.printDebug(response);
        // TODO TREAT RESULT
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("remove FILENAME [FILENAME2 FILENAME3...]", "Remove a file from the cloud");
    }
}
