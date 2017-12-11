package com.dropboxish.services.filters;

import com.dropboxish.db.DropboxishDatabase;
import com.dropboxish.services.KeyGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 *
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = Logger.getLogger("authentication");
    private static final String REALM = "dropboxish";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = trimToken(authorizationHeader);

        try {

            // Validate the token
            validateToken(token);

            String username = DropboxishDatabase.getInstance().getUsername(token);

            if (username == null){
                abortWithUnauthorized(requestContext);
                return;
            }

            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {

                    return () -> username;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }
            });

        } catch (SQLException e) {
            logger.severe(e.getMessage());
            requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("An internal server error occurred.")
                            .build());

        } catch (Exception e) {
            logger.severe(e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {

        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {

        logger.warning("Unauthorized access");
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .entity("You must be authenticated to use this command.")
                        .build());
    }

    /**
     * Remove the html header from the token
     * @param str the {@link String} to trim
     */
    private static String trimToken(String str){
        return str.substring(AUTHENTICATION_SCHEME.length()).trim();
    }

    public static void validateToken(String token) throws Exception {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
        Claims body = Jwts.parser().setSigningKey(KeyGenerator.getKey()).parseClaimsJws(token).getBody();
        // check is the token isn't expired
        assert body.getExpiration().before(new Date());
    }
}
