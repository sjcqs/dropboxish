package com.dropboxish.services;

import com.dropboxish.db.DropboxishDatabase;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.utils.FileUtils;
import com.dropboxish.services.filters.Secured;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
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
                files.removeIf(file -> !file.getFilename().matches(query));
            }
            return Response.ok(FileUtils.serialize(files)).build();
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
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            long written = Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
            if (written != details.getSize()){
                throw new IOException("File isn't complete");
            }

            if (FileUtils.check(path, checksum)){
                // TODO send file to controller and read result
                db.putFile(filename, checksum, size, owner);
                return Response.ok(String.format("%s file was successfully uploaded.", filename))
                        .build();
            } else {
                throw new IOException("Invalid checksum for the file.");
            }
        } catch (IOException | SQLException e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("The files wasn't uploaded.")
                    .build();
        }
    }

    @GET
    @Path("download")
    public Response downloadFile(@QueryParam("filename") String filename){
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Download file : not implemented").build();
    }

    @DELETE
    @Path("remove")
    public Response removeFile(@QueryParam("filenames") String json){
        try {
            String owner = securityContext.getUserPrincipal().getName();
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> names = new Gson().fromJson(json, listType);
            // TODO remove file from controller and read result
            Map<String, Boolean> result =  db.removeFiles(owner, names);
            return Response.ok()
                    .entity(new Gson().toJson(result))
                    .build();
        } catch (SQLException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
        }
    }

}
