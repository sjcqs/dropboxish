package com.dropboxish.controller.grpc;

import com.dropboxish.controller.proto.ControllerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Created by satyan on 12/12/17.
 * Client for the pool GRPC service
 */
public class ControllerClient {
    public static ControllerClient instance = null;

    private final ManagedChannel channel;
    private final ControllerGrpc.ControllerBlockingStub blockingStub;
    private final ControllerGrpc.ControllerStub asyncStub;

    /**
     * Initialize the {@link ControllerClient} instance
     * @param host host of GRPC server
     * @param port port of GRPC server
     * @return initialized {@literal instance}
     */
    public static ControllerClient init(String host, int port){
        instance = new ControllerClient(host, port);
        return instance;
    }

    /**
     * Get a instance of {@link ControllerClient}, or {@code null} if {@link ControllerClient} wasn't init using {@literal init}
     * @return {@literal instance} or {@code null} if not initialized
     */
    public static ControllerClient getInstance(){
        return instance;
    }

    private ControllerClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host,port).usePlaintext(true));
    }

    private ControllerClient(ManagedChannelBuilder<?> builder) {
        channel = builder.build();
        blockingStub = ControllerGrpc.newBlockingStub(channel);
        asyncStub = ControllerGrpc.newStub(channel);
    }

    public ControllerGrpc.ControllerBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public ControllerGrpc.ControllerStub getAsyncStub() {
        return asyncStub;
    }
}
