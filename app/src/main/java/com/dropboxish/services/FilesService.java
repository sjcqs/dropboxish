package com.dropboxish.services;

import com.dropboxish.app.App;
import com.dropboxish.controller.grpc.ControllerClient;
import com.dropboxish.controller.proto.Metadata;
import com.dropboxish.db.DropboxishDatabase;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.services.filters.Secured;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * test
 */
@Path("/file")
@Secured
public class FilesService {
    private final static Logger logger = Logger.getLogger("App");
    @Context
    SecurityContext securityContext;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFiles(@QueryParam("query") final String query) {

        String owner = securityContext.getUserPrincipal().getName();
        try {
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            List<FileInfo> files = db.listFiles(owner);
            if (query != null && !query.isEmpty()) {
                String[] keywords = query.split(" ");
                List<FileInfo> infos = new ArrayList<>(files);
                files.clear();
                for (String keyword : keywords) {
                    for (FileInfo info : infos) {
                        String name = info.getFilename();
                        if (name.contains(keyword)){
                            files.add(info);
                        }
                    }
                }
            }
            return Response.ok(FileUtil.serialize(files)).build();
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("The list of files couldn't be obtained.")
                    .build();
        }
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream stream,
                               @FormDataParam("file")FormDataContentDisposition details,
                               @FormDataParam("checksum") String checksum){
        logger.info(details.getName());
        logger.info(checksum);
        String filename = details.getFileName();
        long size = details.getSize();
        String owner = securityContext.getUserPrincipal().getName();

        try {
            java.nio.file.Path path = Files.createTempFile(filename, null);
            logger.info(path.toString());
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            long written = Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
            if (written != details.getSize()){
                throw new IOException("File isn't complete");
            }

            if (FileUtil.check(path, checksum)){
                logger.info(FileUtil.checksum(path));
                ControllerClient client = App.leader;
                if (client != null) {
                    if (client.putFile(path, Metadata.newBuilder()
                            .setChecksum(checksum)
                            .setFilename(filename)
                            .setOwner(owner)
                            .setLength(size)
                            .build())) {
                        logger.info("OK");
                        db.putFile(filename, checksum, size, owner);
                        return Response.ok(String.format("%s file was successfully uploaded.", filename))
                                .build();
                    } else {
                        logger.warning("Error");
                    }
                }
            } else {
                throw new IOException("Invalid checksum for the file.");
            }
        } catch (IOException | SQLException e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("The files wasn't uploaded.")
                .build();
    }

    @GET
    @Path("download")
    public Response downloadFile(@QueryParam("filename") String filename){
        ControllerClient client = App.leader;
        String owner = securityContext.getUserPrincipal().getName();
        try {
            java.nio.file.Path path = Files.createTempFile(filename, null);
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            FileInfo info = db.getFile(filename, owner);

            if (info != null) {
                logger.info(info.toString());
                client.getFile(info, path);
                StreamingOutput stream = os -> Files.copy(path, os);
                return Response
                        .ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                        .header("content-disposition",String.format("attachment; filename = %s", filename))
                        .build();
            } else{
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("File not found.")
                        .build();
            }
        } catch (SQLException | IOException e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Couldn't download the file.")
                .build();
    }

    @DELETE
    @Path("remove")
    public Response removeFile(@QueryParam("filenames") String json){
        try {
            String owner = securityContext.getUserPrincipal().getName();
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            ControllerClient client = App.leader;
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> names = new Gson().fromJson(json, listType);
            List<String> removed = new ArrayList<>();
            List<FileInfo> files = db.listFiles(owner);
            Map<String, Boolean> result =  db.removeFiles(owner, names);

            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (entry.getValue()){
                    removed.add(entry.getKey());
                }
            }
            files.removeIf(info -> !removed.contains(info.getFilename()));

            for (FileInfo file : files) {
                boolean res = false;
                try {
                    res = client.deleteFile(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put(file.getFilename(), res);
                if (!res){
                    db.putFile(file.getFilename(), file.getChecksum(), file.getSize(), file.getOwner());
                }
            }

            return Response.ok()
                    .entity(new Gson().toJson(result))
                    .build();
        } catch (SQLException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
        }
    }

}
