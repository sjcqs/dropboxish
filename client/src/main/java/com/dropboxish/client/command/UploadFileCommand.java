package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;
import com.dropboxish.client.utils.RequestManager;
import com.dropboxish.model.utils.FileUtil;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by satyan on 11/21/17.
 * Upload a file to the cloud
 */
public class UploadFileCommand extends LoadCommand {
    private final static String PATH = "/file/upload";

    public UploadFileCommand(User user){
        super("upload", user, PATH, HttpMethod.POST);
    }

    @Override
    public void run() throws CommandIllegalArgumentException {
        String filePath = args.get(0);
        Path file = Paths.get(filePath);
        RequestManager rm = getUser().getRequestManager();

        try {
            FileDataBodyPart bodyPart = new FileDataBodyPart(
                    "file",
                    file.toFile(),
                    MediaType.APPLICATION_JSON_TYPE
            );
            bodyPart.setContentDisposition(
                    FormDataContentDisposition.name("file")
                    .fileName(file.getFileName().toString())
                    .size(Files.size(file))
                    .build()
            );
            String checksum = FileUtil.checksum(file);
            MultiPart multiPart = new FormDataMultiPart()
                    .field("checksum", checksum)
                    .bodyPart(bodyPart);
            multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

            Response response = rm.getBase().path(PATH)
                    .request(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()));

            Response.StatusType status = response.getStatusInfo();
            if (!status.getFamily().equals(Response.Status.Family.SUCCESSFUL)){
                if (status == Response.Status.UNAUTHORIZED){
                    rm.disconnect();
                }
                throw new CommandIllegalArgumentException(response.readEntity(String.class));
            }

            String str = response.readEntity(String.class);
            ConsoleUtils.print("UPLOAD", str);
        } catch (IOException | CommandIllegalArgumentException e) {
            ConsoleUtils.printError(filePath + " couldn't be uploaded");
        }
        ConsoleUtils.printPrompt();
    }

    @Override
    public void check() {
        if (args.size() != 1){
            throw new CommandIllegalArgumentException("File to upload not provided.","usage: upload FILE");
        }
        String filePath = args.get(0);
        Path file = Paths.get(filePath);

        if (!Files.exists(file) || !Files.isRegularFile(file)){
            throw new CommandIllegalArgumentException(filePath + " file doesn't exist or is a directory.");
        }
        ConsoleUtils.print("UPLOAD", filePath);
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("upload FILE", "Upload a file to the cloud");
    }
}
