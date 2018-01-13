package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.utils.FileUtil;

import javax.ws.rs.HttpMethod;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    protected ListFilesCommand(User user, String name){
        super(name, user, PATH, HttpMethod.GET);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        String response = sendRequest();
        printFiles(response);
    }

    protected void printFiles(String response) {
        if (response != null){
            List<FileInfo> files = FileUtil.deserializeList(response);
            ConsoleUtils.printTitle("LIST");
            if (files.isEmpty()){
                ConsoleUtils.printShifted("No files.");
            } else {
                for (FileInfo info : files) {
                    FileStatus status = FileStatus.CLOUD;
                    Path file = Paths.get(info.getFilename());
                    if (Files.exists(file)){
                        if (FileUtil.check(file, info.getChecksum())){
                            status = FileStatus.DOWNLOADED;
                        } else {
                            status = FileStatus.MODIFIED;
                        }
                    }
                    ConsoleUtils.printShifted(
                            info.getFilename() + String.format(" (%s)",status.desc),
                            "checksum: " + info.getChecksum(),
                            String.format("size: %d", info.getSize()));
                }
            }
        }
    }


    @Override
    public void help() {
        ConsoleUtils.printShifted("list","List available files (both locally and in the cloud)");
    }


    public enum FileStatus {
        DOWNLOADING("downloading"), DOWNLOADED("downloaded"),
        CLOUD("cloud"), MODIFIED("modified"), UPLOADING("uploading");

        private final String desc;

        FileStatus(String desc) {
            this.desc = desc;
        }
    }
}
