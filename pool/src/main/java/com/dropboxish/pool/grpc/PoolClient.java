package com.dropboxish.pool.grpc;

import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.pool.proto.Block;
import com.dropboxish.pool.proto.OperationStatus;
import com.dropboxish.pool.proto.PoolGrpc;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static void main(String[] args) {
        PoolClient client = new PoolClient("localhost",8060);
        try {
            Block.Builder builder = Block.newBuilder();
            Path path = Paths.get("README.md");
            String checksum = FileUtil.checksum(Paths.get("README.md"));
            System.out.println("Checksum:" + checksum);
            ByteString bytes = ByteString.readFrom(Files.newInputStream(path));
            OperationStatus status = client.blockingStub.putBlock(builder
                    .setLength(Files.size(path))
                    .setChecksum(checksum)
                    .setData(bytes)
                    .build());
            System.out.println(status.getStatus());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
