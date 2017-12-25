package com.dropboxish.controller.grpc;

import com.dropboxish.controller.proto.*;
import com.dropboxish.model.utils.FileUtil;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by satyan on 12/11/17.
 * GRPC service
 */
public class ControllerService extends ControllerGrpc.ControllerImplBase {
    @Override
    public StreamObserver<File> putFile(StreamObserver<OperationStatus> responseObserver) {
        final Metadata.Builder builder = Metadata.newBuilder();
        return new StreamObserver<File>() {
            @Override
            public void onNext(File file) {
                Metadata metadata;
                switch (file.getFileOneofCase()){
                    case METADATA:
                        metadata = file.getMetadata();
                        builder.setFilename(metadata.getFilename())
                                .setChecksum(metadata.getChecksum())
                                .setLength(metadata.getLength());
                        break;
                    case DATA:
                        metadata = builder.build();
                        Data data = file.getData();
                        try (OutputStream out = Files.newOutputStream(
                                Paths.get(metadata.getFilename()), StandardOpenOption.APPEND)){
                            out.write(data.getData().toByteArray(), 0, data.getLength());
                        } catch (IOException e) {
                            responseObserver.onError(e);
                        }
                        break;
                    case FILEONEOF_NOT_SET:
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                Metadata metadata = builder.build();
                String checksum = metadata.getChecksum();
                Path path = Paths.get(metadata.getFilename());
                if (!FileUtil.check(path, checksum)){
                    responseObserver.onNext(OperationStatus.newBuilder()
                            .setStatus(OperationStatus.Status.FAILED)
                            .setReason("Checksum didn't match")
                            .build());
                } else {
                    responseObserver.onNext(OperationStatus.newBuilder()
                            .setStatus(OperationStatus.Status.OK)
                            .build());
                }
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getFile(Metadata request, StreamObserver<File> responseObserver) {
        try {
            Path path = Files.createTempFile(request.getFilename(), null);
            // TODO retrieve blocks from pools, construct file, check metadata
            if (FileUtil.check(path, request.getChecksum())) {
                byte[] bytes = new byte[1_000_000];
                try (InputStream in = Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE)) {
                    int read;
                    while ((read = in.read(bytes, 0, 1_000_000)) != -1) {
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        Data data = Data.newBuilder()
                                .setData(ByteString.copyFrom(buffer))
                                .setLength(read)
                                .build();
                        File file = File.newBuilder()
                                .setData(data)
                                .build();
                        responseObserver.onNext(file);
                    }
                }
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new FileNotFoundException("File doesn't exists."));
            }
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteFile(Metadata request, StreamObserver<OperationStatus> responseObserver) {
        responseObserver.onError(new UnsupportedOperationException("Not implemented"));
    }
}
