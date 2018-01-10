package com.dropboxish.app;

import com.dropboxish.controller.grpc.ControllerClient;
import com.dropboxish.controller.proto.Leader;
import com.dropboxish.controller.proto.Subscription;
import com.dropboxish.db.DropboxishDatabase;
import io.grpc.stub.StreamObserver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Server for the Rest API
 */
public class App extends ResourceConfig{
    private static Logger logger = Logger.getLogger("App");
    private final List<ControllerClient> controllers = new ArrayList<>();
    public static ControllerClient leader = null;

    public App(String[] args) {
        packages("com/dropboxish/services");
        register(MultiPartFeature.class);
        assert isRegistered(MultiPartFeature.class);

        if (args.length < 1) {
            System.err.println("Missing at least one controller address.");
            System.exit(-1);
        }

        for (String arg : args) {
            String addr[] = arg.split(":");
            String host = addr[0];
            int port = Integer.valueOf(addr[1]);

            final ControllerClient c = new ControllerClient(host, port);

            new Thread(new Runnable() {
                boolean connected = false;
                @Override
                public void run() {
                    controllers.add(c);
                    while (true) {
                        if (!connected) {
                            connected = true;
                            c.getAsyncStub().withWaitForReady()
                                    .getLeader(Subscription.getDefaultInstance(), new StreamObserver<Leader>() {
                                        @Override
                                        public void onNext(Leader value) {
                                            leader = c;
                                            logger.info("Leader: " + c);
                                        }

                                        @Override
                                        public void onError(Throwable t) {
                                            logger.warning(t.getMessage());
                                            connected = false;
                                            if (leader.equals(c)){
                                                leader = null;
                                                logger.warning("No leader.");
                                            }
                                        }

                                        @Override
                                        public void onCompleted() {
                                            logger.info("Completed");
                                            connected = false;
                                        }
                                    });
                        } else {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }}).start();
        }
    }

    public ControllerClient getLeader() {
        return leader;
    }

    public static void main(String[] args) {
        ResourceConfig app = new App(args);

        ServletHolder servlet = new ServletHolder(new ServletContainer(app));
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
