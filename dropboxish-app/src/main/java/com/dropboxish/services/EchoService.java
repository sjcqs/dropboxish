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
@Path("/echo")
public class EchoService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response echo(@QueryParam("message") String msg) {
        return Response.ok().entity(msg == null ? "no message" : msg).build();
    }

    @GET
    @Secured
    @Path("jwt")
    public Response echoJWT(@QueryParam("message") String msg,
                            @Context SecurityContext securityContext){
        Principal principal = securityContext.getUserPrincipal();
        String username = principal.getName();
        return Response.ok().entity(username + ((msg == null) ? "no message" : msg)).build();
    }

}
