package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.utils.FileUtil;

import javax.ws.rs.HttpMethod;
import java.util.List;

/**
 * Created by satyan on 11/21/17.
 * List available files (both locally and in the cloud)
 */
public class ListFilesCommand extends RestCommand {
    private final static String PATH = "/file/list";
    public ListFilesCommand(User user) {
        super("list", user, PATH, HttpMethod.GET);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        String response = sendRequest();
        if (response != null){
            List<FileInfo> files = FileUtil.deserializeList(response);
            ConsoleUtils.printTitle("LIST");
            if (files.isEmpty()){
                ConsoleUtils.printShifted("No files.");
            } else {
                for (FileInfo file : files) {
                    ConsoleUtils.printShifted(
                            file.getFilename(),
                            "checksum: " + file.getChecksum(),
                            String.format("size: %d", file.getSize()));
                }
            }
        }
    }



    @Override
    public void help() {
        ConsoleUtils.printShifted("list","List available files (both locally and in the cloud)");
    }
}
