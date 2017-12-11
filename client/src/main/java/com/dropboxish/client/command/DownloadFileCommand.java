package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import javax.ws.rs.HttpMethod;

/**
 * Created by satyan on 11/21/17.
 * Allow to download a file available in the cloud
 */
public class DownloadFileCommand extends RestCommand {
    private final static String PATH = "/file/download";
    public DownloadFileCommand(User user){
        super("download", user, PATH, HttpMethod.GET);

    }

    @Override
    public void run() throws IllegalArgumentException{
        if (args.size() == 0){
            throw new CommandIllegalArgumentException("A file name must be provided","Usage: download FILE");
        }
        ConsoleUtils.printTitle("DOWNLOAD");
        ConsoleUtils.printShifted("Downloading file", args.get(0));
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("download FILE", "Download a file from the cloud.");
    }
}
