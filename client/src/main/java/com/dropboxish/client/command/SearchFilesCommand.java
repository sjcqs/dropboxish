package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.utils.FileUtil;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by satyan on 11/21/17.
 * Search file whose name contains PATTERN
 */
public class SearchFilesCommand extends RestCommand {
    private final static String PATH = "/file/list";
    public SearchFilesCommand(User user) {
        super("search", user, PATH, HttpMethod.GET);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        if (args.isEmpty()){
            throw new CommandIllegalArgumentException("Missing the search pattern.");
        }
        Map<String, String> params = new HashMap<>();
        String query = "";
        for (String arg : args) {
            query += arg + " ";
        }
        params.put("query", query.trim());
        String response = sendRequest(params);
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
        ConsoleUtils.printShifted("search PATTERN [PATTERN2...]", "Search file whose name contains PATTERN");
    }
}
