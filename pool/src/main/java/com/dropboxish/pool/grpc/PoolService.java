package com.dropboxish.pool.grpc;

import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.pool.proto.Block;
import com.dropboxish.pool.proto.BlockRequest;
import com.dropboxish.pool.proto.OperationStatus;
import com.dropboxish.pool.proto.PoolGrpc;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by satyan on 12/11/17.
 * GRPC service
 */
public class PoolService extends PoolGrpc.PoolImplBase {
    private static final String FILE_EXTENSION = ".block";

    @Override
    public void getBlock(BlockRequest request, StreamObserver<Block> responseObserver) {
        final String checksum = request.getChecksum();
        final Path path = Paths.get(checksum, FILE_EXTENSION);

        if (Files.exists(path)){
            try {
                InputStream stream = Files.newInputStream(path);
                responseObserver.onNext(Block.newBuilder()
                        .setLength(Files.size(path))
                        .setChecksum(checksum)
                        .setData(ByteString.readFrom(stream))
                        .build());
                responseObserver.onCompleted();
            } catch (IOException e) {
                responseObserver.onError(e);
            }
        } else {
            responseObserver.onError(new NoSuchFileException("Block doesn't exists"));
        }
    }

    @Override
    public void putBlock(Block request, StreamObserver<OperationStatus> responseObserver) {
        final String checksum = request.getChecksum();
        final Path file = Paths.get(checksum, FILE_EXTENSION);
        final ByteString data = request.getData();
        final long length = request.getLength();

        ByteBuffer buffer = data.asReadOnlyByteBuffer();
        if (data.size() == length && FileUtil.check(buffer, checksum)){
            try(OutputStream out = Files.newOutputStream(file)){
                out.write(buffer.array());
                responseObserver.onNext(OperationStatus.newBuilder()
                        .setStatus(OperationStatus.Status.OK)
                        .build());
            } catch (IOException e) {
                responseObserver.onNext(OperationStatus.newBuilder()
                        .setStatus(OperationStatus.Status.FAILED)
                        .setReason(e.getMessage())
                        .build());
            }
        } else {
            responseObserver.onNext(OperationStatus.newBuilder()
                    .setStatus(OperationStatus.Status.FAILED)
                    .setReason("Block checksum doesn't match")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlock(BlockRequest request, StreamObserver<OperationStatus> responseObserver) {
        final String checksum = request.getChecksum();
        final Path path = Paths.get(checksum, FILE_EXTENSION);

        
        if (Files.exists(path)){
            try {
                Files.delete(path);
                responseObserver.onNext(OperationStatus.newBuilder()
                        .setStatus(OperationStatus.Status.OK)
                        .build());
            } catch (IOException e) {
                responseObserver.onNext(OperationStatus.newBuilder()
                .setStatus(OperationStatus.Status.FAILED)
                .setReason(e.getMessage())
                .build());
            }
            responseObserver.onCompleted();
        } else {
            responseObserver.onNext(OperationStatus.newBuilder()
                    .setStatus(OperationStatus.Status.FAILED)
                    .setReason("Block doesn't exists")
                    .build());
        }
    }
}
