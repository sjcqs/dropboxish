package com.dropboxish.services;

import com.dropboxish.db.UserDatabase;
import com.dropboxish.services.filters.AuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 *
 */

@Path("/user")
public class AuthenticationService {

    private final static Logger logger = Logger.getLogger("Authentification Service");

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(@QueryParam("username") String login,
                                     @QueryParam("password") String password){
        try {
            // Authenticate the user using the credentials provided
            authenticate(login, password);

            // Issue a token for the user
            String token = getToken(login);
            assert token != null;

            // Return the token on the response
            return Response.ok(token).build();

        } catch (IllegalArgumentException e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.FORBIDDEN).entity("Login/password is/are wrong.").build();
        } catch (SQLException | UnsupportedEncodingException e) {
            logger.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error issuing the token").build();
        }
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@QueryParam("username") String login,
                                 @QueryParam("password") String password){
        try {
            UserDatabase db = UserDatabase.getInstance();

            if (db.isUsernameAvailable(login)) {

                // Issue a token for the user
                String token = issueToken(login);

                logger.info("Registration: " + login + " " + token);

                db.createUser(login, password, token);

                // Return the token on the response
                System.err.println("OK");
                return Response.ok(token).build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("Username is already used.").build();
            }

        } catch (Exception e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void authenticate(String login, String password) throws IllegalArgumentException, SQLException {
        UserDatabase db = UserDatabase.getInstance();

        if (!db.checkCredentials(login, password)){ // credentials are wrong
            throw new IllegalArgumentException("Login/password is/are wrong.");
        }
    }

    private String issueToken(String username) throws UnsupportedEncodingException {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(toDate(LocalDateTime.now().plusMinutes(15L)))
                .signWith(SignatureAlgorithm.HS512, KeyGenerator.getKey())
                .compact();
    }

    private String getToken(String username) throws UnsupportedEncodingException, SQLException {
        UserDatabase db = UserDatabase.getInstance();
        String token = db.getToken(username);

        if (token != null){ // check the validity
            try {
                AuthenticationFilter.validateToken(token);
            } catch (Exception e) {
                token =  Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(new Date())
                        .setExpiration(toDate(LocalDateTime.now().plusMinutes(15L)))
                        .signWith(SignatureAlgorithm.HS512, KeyGenerator.getKey())
                        .compact();
                boolean res = db.updateToken(username, token);
                if (!res){
                    throw new SQLException();
                }
            }
        }

        return token;
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
