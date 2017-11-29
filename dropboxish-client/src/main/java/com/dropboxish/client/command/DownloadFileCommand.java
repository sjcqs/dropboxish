package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.ConsoleUtils;

/**
 * Created by satyan on 11/21/17.
 * Allow to download a file available in the cloud
 */
public class DownloadFileCommand extends ClientCommand {
    public DownloadFileCommand(Client client){
        super(client, "download");

    }

    @Override
    public void run(){
        if (args == null || args.size() == 0){
            ConsoleUtils.printError(
                    "A file name must be provided",
                    "Usage: download FILE"
            );
            return;
        }
        ConsoleUtils.printTitle("DOWNLOAD");
        //TODO implementation
        ConsoleUtils.printShifted("Downloading file",args.get(0));
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("download FILE", "Download a file from the cloud.");
    }
}
