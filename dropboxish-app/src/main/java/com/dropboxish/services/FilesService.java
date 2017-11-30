package com.dropboxish.services;

import com.dropboxish.services.filters.Secured;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created by satyan on 11/22/17.
 * test
 */
@Path("/file")
@Secured
public class FilesService {
    @Context
    SecurityContext securityContext;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFiles(@QueryParam("query") String query) {
        if (query == null || query.isEmpty()) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).entity("List files : not implemented").build();
        } else {
            return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Search files : not implemented").build();
        }
    }

    @POST
    @Path("upload")
    public Response uploadFile(@QueryParam("file") String tmp){
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Store file : not implemented").build();
    }

    @GET
    @Path("download")
    public Response downloadFile(@QueryParam("filename") String filename){
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Download file : not implemented").build();
    }

    @DELETE
    @Path("remove")
    public Response removeFile(@QueryParam("filename") String filename){
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Delete file : not implemented").build();
    }

}
