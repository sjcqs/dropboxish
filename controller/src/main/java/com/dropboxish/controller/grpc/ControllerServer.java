package com.dropboxish.controller.grpc;

import com.dropboxish.pool.grpc.RpcServer;
import io.grpc.ServerBuilder;

import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * GRPC Server to receive controller related requests
 */
public class ControllerServer extends RpcServer{
    private final static Logger logger = Logger.getLogger("Controller");

    public ControllerServer(int port){
        this(ServerBuilder.forPort(port), port);
    }

    private ControllerServer(ServerBuilder<?> serverBuilder, int port) {
        super(serverBuilder, port, new ControllerService());
    }
}
