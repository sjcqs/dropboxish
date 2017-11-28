package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 * Upload a file to the cloud
 */
public class UploadFileCommand extends ClientCommand {

    public UploadFileCommand(Client client){
        super(client,"upload");
    }

    @Override
    public void run() throws IllegalArgumentException {
        // TODO implementation
    }

    @Override
    public void help() {
        ConsoleUtils.printLines("upload FILE", "Upload a file to the cloud");
    }
}
