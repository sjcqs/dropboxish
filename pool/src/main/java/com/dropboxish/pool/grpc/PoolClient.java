package com.dropboxish.pool.grpc;

import com.dropboxish.model.Host;
import com.dropboxish.pool.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
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
    private final ManagedChannel channel;

    public PoolClient(String host, int port){
        this(new Host(host, port));
    }

    public PoolClient(Host host){
        ManagedChannelBuilder<?> builder =
                ManagedChannelBuilder.forAddress(host.getHost(),host.getPort()).usePlaintext(true);
        channel = builder.build();
        blockingStub = PoolGrpc.newBlockingStub(channel);
        asyncStub = PoolGrpc.newStub(channel);
        this.host = host;
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

    public Runnable getBlock(ByteBuffer buffer, String checksum) {
        return new Downloader(buffer, checksum);
    }

    public boolean deleteBlock(String checksum) {
        BlockRequest request = BlockRequest.newBuilder().setChecksum(checksum).build();
        return blockingStub.deleteBlock(request).getStatus().equals(OperationStatus.Status.OK);
    }

    @Override
    public String toString() {
        return host.toString();
    }

    private class Downloader implements Runnable{
        private  final ByteBuffer buffer;
        private final BlockRequest request;

        private Downloader(ByteBuffer buffer, String checksum) {
            this.buffer = buffer;
            this.request = BlockRequest.newBuilder()
                    .setChecksum(checksum)
                    .build();
        }

        @Override
        public void run() {
            try {
                Iterator<Block> it = blockingStub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .withWaitForReady().getBlock(request);

                while (it.hasNext()) {
                    Block block = it.next();
                    Data data = block.getData();
                    buffer.put(data.getData().toByteArray(), 0, data.getLength());
                }
            } catch (Exception e){
                logger.warning(e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
