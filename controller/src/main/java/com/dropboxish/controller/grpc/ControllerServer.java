package com.dropboxish.controller.grpc;

import com.dropboxish.controller.Controller;
import com.dropboxish.pool.grpc.PoolClient;
import com.dropboxish.pool.grpc.RpcServer;
import io.grpc.ServerBuilder;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * GRPC Server to receive controller related requests
 */
public class ControllerServer extends RpcServer{
    private final static Logger logger = Logger.getLogger("Controller");

    public ControllerServer(Controller controller, int port, List<PoolClient> pools){
        this(ServerBuilder.forPort(port), controller, port, pools);
    }

    private ControllerServer(ServerBuilder<?> serverBuilder, Controller controller, int port, List<PoolClient> pools) {
        super(serverBuilder, port, new ControllerService(controller, pools));
    }
}
