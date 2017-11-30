package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

/**
 * Created by satyan on 11/21/17.
 * List available files (both locally and in the cloud)
 */
public class ListFilesCommand extends RestCommand {
    private final static String PATH = "/file/list";
    public ListFilesCommand(Client client) {
        super("list", client, PATH, HttpMethod.GET, true);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        ContentResponse response = sendRequest();
        if (response != null){

        }
    }



    @Override
    public void help() {
        ConsoleUtils.printShifted("list","List available files (both locally and in the cloud)");
    }
}
