package com.dropboxish.controller.grpc;

import com.dropboxish.controller.proto.ControllerGrpc;
import com.dropboxish.controller.proto.File;
import com.dropboxish.controller.proto.Metadata;
import com.dropboxish.controller.proto.OperationStatus;
import com.dropboxish.model.Host;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/12/17.
 * Client for the pool GRPC service
 */
public class ControllerClient {

    private static final Logger logger = Logger.getLogger("Client");
    private final ManagedChannel channel;
    private final ControllerGrpc.ControllerBlockingStub blockingStub;
    private final ControllerGrpc.ControllerStub asyncStub;
    private Host host;


    public ControllerClient(String host, int port){
        this(new Host(host, port));
    }

    public ControllerClient(Host host){
        this.host = host;
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(host.getHost(),host.getPort()).usePlaintext(true);
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

    public ConnectivityState getState(){
        return  channel.getState(false);
    }

    public boolean putFile(Path path, Metadata metadata){
        final OperationStatus.Builder builder = OperationStatus.newBuilder();
        builder.setStatus(OperationStatus.Status.UNKNOWN);
        StreamObserver<File> requestObserver = asyncStub.putFile(
                new StreamObserver<OperationStatus>() {
                    @Override
                    public void onNext(OperationStatus value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.warning("Error:" + t.getMessage());
                        builder.setStatus(OperationStatus.Status.FAILED)
                                .setReason(t.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Completed");
                        builder.setStatus(OperationStatus.Status.OK);
                    }
                });
        try {
            requestObserver.onNext(File.newBuilder().setMetadata(metadata).build());
            ControllerService.sendFile(path, requestObserver);
            // wait for the response
            while(builder.getStatus().equals(OperationStatus.Status.UNKNOWN)){
                try {
                    logger.info("WAIT");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.info("INTERRUPTION");
                    Thread.interrupted();
                }
                logger.info(builder.getStatus().name());
            }
        } catch (IOException e) {
            return false;
        }

        return builder.getStatus().equals(OperationStatus.Status.OK);

    }

    @Override
    public String toString() {
        return String.format("%s", host);
    }
}
