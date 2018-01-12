package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/21/17.
 * Allow to download a file available in the cloud
 */
public class DownloadFileCommand extends RestCommand {
    private final static String PATH = "/file/download";
    public DownloadFileCommand(User user){
        super("download", user, PATH, HttpMethod.GET, true);

    }

    @Override
    public void run() throws IllegalArgumentException{
        final String filename = args.get(0);
        Map<String, String> params = new HashMap<>();
        params.put("filename", filename);
        try {
            Response response = sendRequest2(params);

            InputStream stream = (InputStream) response.getEntity();
            Path path = Paths.get(filename);
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
            ConsoleUtils.print("DOWNLOAD", filename + " was successfully downloaded.");
        } catch (IOException | CommandIllegalArgumentException e){
            ConsoleUtils.printError(filename + " couldn't be downloaded");
        }
        ConsoleUtils.printPrompt();
    }

    @Override
    public void check() {
        if (args.size() == 0){
            throw new CommandIllegalArgumentException("A file name must be provided","Usage: download FILE");
        }
        final String filename = args.get(0);

        ConsoleUtils.print("DOWNLOAD", filename);
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("download FILE", "Download a file from the cloud.");
    }
}
