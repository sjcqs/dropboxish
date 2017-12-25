package com.dropboxish.pool.grpc;

import com.dropboxish.pool.proto.PoolGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Created by satyan on 12/12/17.
 * Client for the pool GRPC service
 */
public class PoolClient {
    public static PoolClient instance = null;

    private final ManagedChannel channel;
    private final PoolGrpc.PoolBlockingStub blockingStub;
    private final PoolGrpc.PoolStub asyncStub;

    /**
     * Initialize the {@link PoolClient} instance
     * @param host host of GRPC server
     * @param port port of GRPC server
     * @return initialized {@literal instance}
     */
    public static PoolClient init(String host, int port){
        instance = new PoolClient(host, port);
        return instance;
    }

    /**
     * Get a instance of {@link PoolClient}, or {@code null} if {@link PoolClient} wasn't init using {@literal init}
     * @return {@literal instance} or {@code null} if not initialized
     */
    public static PoolClient getInstance(){
        return instance;
    }

    private PoolClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host,port).usePlaintext(true));
    }

    private PoolClient(ManagedChannelBuilder<?> builder) {
        channel = builder.build();
        blockingStub = PoolGrpc.newBlockingStub(channel);
        asyncStub = PoolGrpc.newStub(channel);
    }

    public PoolGrpc.PoolBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public PoolGrpc.PoolStub getAsyncStub() {
        return asyncStub;
    }
}
