package com.dropboxish.pool.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * GRPC Server to receive pool related requests
 */
public abstract class RpcServer {
    public static final int GRPC_MAX_SIZE = 4_000_000;
    private final static Logger logger = Logger.getLogger("Pool");
    private final int port;
    private final Server server;

    protected RpcServer(ServerBuilder<?> serverBuilder, int port, BindableService service) {
        this.port = port;
        this.server = serverBuilder.addService(service)
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may has been reset by its JVM shutdown hook.
            System.err.println("Shutting down gRPC server");
            RpcServer.this.stop();
            System.err.println("Server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Block until the JVM is shutdown
     * @throws InterruptedException if the thread is interrupted
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
