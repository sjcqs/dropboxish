package com.dropboxish.pool.grpc;

import com.dropboxish.model.Host;
import com.dropboxish.pool.proto.Block;
import com.dropboxish.pool.proto.Metadata;
import com.dropboxish.pool.proto.OperationStatus;
import com.dropboxish.pool.proto.PoolGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/12/17.
 * Client for the pool GRPC service
 */
public class PoolClient {
    private static final Logger logger = Logger.getLogger("Pool");
    private final PoolGrpc.PoolBlockingStub blockingStub;
    private final PoolGrpc.PoolStub asyncStub;
    private final Host host;

    public PoolClient(String host, int port){
        this(new Host(host, port));
    }

    public PoolClient(Host host){
        ManagedChannelBuilder<?> builder =
                ManagedChannelBuilder.forAddress(host.getHost(),host.getPort()).usePlaintext(true);
        ManagedChannel channel = builder.build();
        blockingStub = PoolGrpc.newBlockingStub(channel);
        asyncStub = PoolGrpc.newStub(channel);
        this.host = host;
    }

    public PoolGrpc.PoolBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public PoolGrpc.PoolStub getAsyncStub() {
        return asyncStub;
    }


    public Host getHost() {
        return host;
    }

    public boolean putBlock(ByteBuffer buffer, Metadata metadata) {
        final OperationStatus.Builder builder = OperationStatus.newBuilder();
        builder.setStatus(OperationStatus.Status.UNKNOWN);

        StreamObserver<Block> requestObserver = asyncStub.putBlock(
                new StreamObserver<OperationStatus>() {
                    @Override
                    public void onNext(OperationStatus value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.warning(t.getMessage());
                        builder.setStatus(OperationStatus.Status.FAILED)
                                .setReason(t.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Block sent");
                        builder.setStatus(OperationStatus.Status.OK);
                    }
                });
        try {
            requestObserver.onNext(Block.newBuilder()
                    .setMetadata(metadata)
                    .build());
            PoolService.sendBlock(buffer, requestObserver);
            // wait for the response
            while(builder.getStatus().equals(OperationStatus.Status.UNKNOWN)){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        } catch (IOException e) {
            return false;
        }
        return !builder.getStatus().equals(OperationStatus.Status.FAILED);
    }
}
