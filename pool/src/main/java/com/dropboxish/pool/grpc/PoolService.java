package com.dropboxish.pool.grpc;

import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.pool.proto.*;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.
 * GRPC service
 */
public class PoolService extends PoolGrpc.PoolImplBase {
    private static final String FILE_EXTENSION = ".block";
    private static final Logger logger = Logger.getLogger("Pool");

    private static Path getPath(String checksum){
        Path path = Paths.get("store",checksum + FILE_EXTENSION);
        path.toFile().getParentFile().mkdirs();
        return path;
    }

    @Override
    public void getBlock(BlockRequest request, StreamObserver<Block> responseObserver) {
        final String checksum = request.getChecksum();
        final Path path = getPath(checksum);

        if (Files.exists(path) && FileUtil.check(path, checksum)){
            try {
                sendBlock(path, responseObserver);
            } catch (IOException e) {
                responseObserver.onError(e);
            }
        } else {
            responseObserver.onError(new NoSuchFileException("Block doesn't exists or is corrupted."));
        }
    }

    public static void sendBlock(Path path, StreamObserver<Block> responseObserver) throws IOException {
        byte[] bytes = new byte[RpcServer.GRPC_MAX_SIZE];
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE)) {
            int read;
            while ((read = in.read(bytes, 0, RpcServer.GRPC_MAX_SIZE)) != -1) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                Data data = Data.newBuilder()
                        .setData(ByteString.copyFrom(buffer))
                        .setLength(read)
                        .build();
                Block file = Block.newBuilder()
                        .setData(data)
                        .build();
                responseObserver.onNext(file);
            }
            responseObserver.onCompleted();
        }
    }

    public static void sendBlock(ByteBuffer buff, StreamObserver<Block> responseObserver) throws IOException {
        while (buff.hasRemaining()) {
            int read = Math.min(RpcServer.GRPC_MAX_SIZE, buff.remaining());
            ByteString bytes = ByteString.copyFrom(buff, read);
            Data data = Data.newBuilder()
                    .setData(bytes)
                    .setLength(read)
                    .build();
            Block block = Block.newBuilder()
                    .setData(data)
                    .build();
            responseObserver.onNext(block);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Block> putBlock(StreamObserver<OperationStatus> responseObserver) {
        final Metadata.Builder builder = Metadata.newBuilder();
        return new StreamObserver<Block>() {
            @Override
            public void onNext(Block block) {
                Metadata metadata;
                switch (block.getFileOneofCase()){
                    case METADATA:
                        metadata = block.getMetadata();
                        try {
                            Files.deleteIfExists(getPath(metadata.getChecksum()));
                            builder.setChecksum(metadata.getChecksum())
                                    .setLength(metadata.getLength());
                        } catch (IOException e) {
                            responseObserver.onError(e);
                        }
                        break;
                    case DATA:
                        metadata = builder.build();
                        Data data = block.getData();
                        try (OutputStream out = Files.newOutputStream(
                                getPath(metadata.getChecksum()), StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
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
                Path path = getPath(checksum);
                if (!FileUtil.check(path, checksum)){
                    logger.info("Block NOT uploaded: " + checksum + " | " + FileUtil.checksum(path));
                    responseObserver.onError(new IOException("Checksum doesn't match"));
                } else {
                    logger.info("Block uploaded: " + checksum);
                    responseObserver.onNext(OperationStatus.newBuilder()
                            .setStatus(OperationStatus.Status.OK)
                            .build());
                    responseObserver.onCompleted();
                }
            }
        };
    }

    @Override
    public void deleteBlock(BlockRequest request, StreamObserver<OperationStatus> responseObserver) {
        final String checksum = request.getChecksum();
        final Path path = getPath(checksum);

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
