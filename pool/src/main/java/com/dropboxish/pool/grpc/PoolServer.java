package com.dropboxish.pool.grpc;

import io.grpc.ServerBuilder;

import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * GRPC Server to receive pool related requests
 */
public class PoolServer extends RpcServer{
    private final static Logger logger = Logger.getLogger("Pool");

    public PoolServer(int port){
        this(ServerBuilder.forPort(port), port);
    }

    public PoolServer(ServerBuilder<?> serverBuilder, int port) {
        super(serverBuilder, port, new PoolService());
    }
}
