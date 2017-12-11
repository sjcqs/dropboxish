package com.dropboxish.app;

import com.dropboxish.db.DropboxishDatabase;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Server for the Rest API
 */
public class App{
    private static Logger logger = Logger.getLogger("App");

    public static void main(String[] args) {
        ResourceConfig config = new ResourceConfig();
        config.packages("com/dropboxish/services");
        config.register(MultiPartFeature.class);
        assert config.isRegistered(MultiPartFeature.class);

        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(server,"/*");
        context.addServlet(servlet,"/*");

        // init the database
        try {
            DropboxishDatabase.init();
        } catch (SQLException e) {
            logger.severe("Database's initialization failed.");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.destroy();
        }
    }
}
