package com.dropboxish.controller.grpc;

import com.dropboxish.controller.proto.*;
import com.dropboxish.model.FileInfo;
import com.dropboxish.model.Host;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        } catch (IOException e) {
            return false;
        }

        return builder.getStatus().equals(OperationStatus.Status.OK);

    }

    public boolean getFile(FileInfo info, Path path){
        Metadata metadata = Metadata.newBuilder()
                .setChecksum(info.getChecksum())
                .setLength(info.getSize())
                .setOwner(info.getOwner())
                .setFilename(info.getFilename())
                .build();
        try {
            Iterator<File> it = blockingStub.getFile(metadata);
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
                while (it.hasNext()) {
                    File file = it.next();
                    Data data = file.getData();
                    out.write(data.getData().toByteArray(), 0, data.getLength());
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s", host);
    }

    public boolean deleteFile(FileInfo file) throws Exception{
        Metadata metadata = Metadata.newBuilder()
                .setFilename(file.getFilename())
                .setChecksum(file.getChecksum())
                .setOwner(file.getOwner())
                .setLength(file.getSize())
                .build();
        return blockingStub.deleteFile(metadata).getStatus().equals(OperationStatus.Status.OK);
    }
}
