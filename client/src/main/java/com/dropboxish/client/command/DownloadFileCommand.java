package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by satyan on 11/21/17.
 * Allow to download a file available in the cloud
 */
public class DownloadFileCommand extends LoadCommand {
    private final static String PATH = "/file/download";
    public DownloadFileCommand(User user){
        super("download", user, PATH, HttpMethod.GET);

    }

    @Override
    public void run() throws IllegalArgumentException{
        String filename = args.get(0);
        Map<String, String> params = new HashMap<>();
        params.put("filename", filename);
        try {
            Response response = sendRequest2(params);

            InputStream stream = (InputStream) response.getEntity();
            Path path;
            if (args.size() > 1){
                Path dest = Paths.get(args.get(1));
                if(Files.isDirectory(dest)) {
                    path = dest.resolve(filename);
                } else {
                    path = dest;
                }
            } else {
                path = Paths.get(filename);
            }
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE)){
                byte[] buffer = new byte[4096];
                int read;
                while ((read = stream.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            }
            ConsoleUtils.print("DOWNLOAD", path.toString() + " was successfully downloaded.");
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
        String filename = args.get(0);

        ConsoleUtils.print("DOWNLOAD", String.format("Download of %s started.",filename));
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("download FILE [DEST]", "Download FILE from the cloud.",
                "Put it in the current directory or in DEST.");
    }
}
