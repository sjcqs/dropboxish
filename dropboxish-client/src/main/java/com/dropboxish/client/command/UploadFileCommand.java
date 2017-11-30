package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;
import org.eclipse.jetty.http.HttpMethod;

/**
 * Created by satyan on 11/21/17.
 * Upload a file to the cloud
 */
public class UploadFileCommand extends RestCommand {
    private final static String PATH = "/file/upload";

    public UploadFileCommand(Client client){
        super("upload", client, PATH, HttpMethod.POST, true);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        // TODO implementation
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("upload FILE", "Upload a file to the cloud");
    }
}
