package com.dropboxish.controller.grpc;

import com.dropboxish.controller.Controller;
import com.dropboxish.controller.fec.FECUtil;
import com.dropboxish.controller.proto.*;
import com.dropboxish.controller.state.Block;
import com.dropboxish.controller.state.ChunkInfo;
import com.dropboxish.controller.state.FileInfo;
import com.dropboxish.controller.state.Update;
import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.pool.grpc.PoolClient;
import com.dropboxish.pool.grpc.RpcServer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.p
 * GRPC service
 */
public class ControllerService extends ControllerGrpc.ControllerImplBase {
    private  static final Logger logger = Logger.getLogger("ControllerService");
    private final Controller controller;
    private final List<PoolClient> pools;

    private static Path getPath(String name){
        return Paths.get(name + ".tmp");
    }

    public ControllerService(Controller controller, List<PoolClient> pools) {
        this.controller = controller;
        this.pools = pools;
    }

    @Override
    public StreamObserver<File> putFile(StreamObserver<OperationStatus> responseObserver) {
        final Metadata.Builder builder = Metadata.newBuilder();
        return new StreamObserver<File>() {
            @Override
            public void onNext(File file) {
                Metadata metadata;
                switch (file.getFileOneofCase()){
                    case METADATA:
                        // TODO check if file already exists and if checksum didn't change
                        metadata = file.getMetadata();
                        builder.setFilename(metadata.getFilename())
                                .setChecksum(metadata.getChecksum())
                                .setLength(metadata.getLength());
                        break;
                    case DATA:
                        metadata = builder.build();
                        Data data = file.getData();
                        try (OutputStream out = Files.newOutputStream(
                                getPath(metadata.getFilename()), StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
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
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                Metadata metadata = builder.build();
                String checksum = metadata.getChecksum();
                Path path = getPath(metadata.getFilename());
                if (!FileUtil.check(path, checksum)){
                    logger.info("File NOT uploaded: " + metadata.getFilename() + " " + checksum + " | " + FileUtil.checksum(path));
                    responseObserver.onError(new IOException("Error receiving file"));
                } else {
                    logger.info("File uploaded: " + metadata.getFilename() + " " + checksum);
                    FileInfo info = sendToPools(path, metadata.getFilename(), metadata.getChecksum(), metadata.getLength());
                    if(info != null) {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            logger.warning(e.getMessage());
                        }
                        try {
                            controller.update(new Update(Update.Type.ADD_FILE, info));
                            //TODO add to local database
                            responseObserver.onNext(OperationStatus.newBuilder()
                                    .setStatus(OperationStatus.Status.OK)
                                    .build());
                            logger.info(info.toString());
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            logger.warning(e.getMessage());
                            responseObserver.onError(new IOException("File not uploaded"));
                        }

                    } else {
                        responseObserver.onError(new IOException("Unable to upload blocks."));
                    }
                }
            }
        };
    }

    private FileInfo sendToPools(Path file, String filename, String checksum, long length) {
        FileInfo info = new FileInfo(filename,checksum,length);
        try {
            Path chunks[] = FileUtil.chunks(file);
            for (int i = 0; i < chunks.length; i++) {
                Path path = chunks[i];
                ChunkInfo chunk = new ChunkInfo(i, FileUtil.checksum(path), Files.size(path));
                Block[] blocks = FECUtil.encode(path, pools.size());
                List<PoolClient> list = new ArrayList<>(pools);
                Random rand = new Random();
                for (Block block : blocks) {
                    if (block != null) {
                        int index = rand.nextInt(list.size());
                        PoolClient pool = list.remove(index);
                        if (pool.putBlock(
                                block.getBuffer(),
                                com.dropboxish.pool.proto.Metadata.newBuilder()
                                        .setChecksum(block.getChecksum())
                                        .setLength(block.getLength())
                                        .build())){
                            logger.info("Block sent to " + pool.getHost());
                            chunk.putBlock(block.getInfo(), pool.getHost());
                        } else {
                            logger.warning("Block could'nt be sent to " + pool.getHost());
                            return null;
                        }
                    }
                }
                info.addChunk(chunk);
            }
        } catch (IOException e) {
            return null;
        }
        return info;
    }

    @Override
    public void getFile(Metadata request, StreamObserver<File> responseObserver) {
        try {
            Path path = Files.createTempFile(request.getFilename(), null);
            // TODO retrieve blocks from pools, construct file, check metadata
            if (FileUtil.check(path, request.getChecksum())) {
                ControllerService.sendFile(path, responseObserver);
            } else {
                responseObserver.onError(new FileNotFoundException("File doesn't exists."));
            }
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    public static void sendFile(Path path, StreamObserver<File> responseObserver) throws IOException {
        byte[] bytes = new byte[RpcServer.GRPC_MAX_SIZE];
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE)) {
            int read;
            while ((read = in.read(bytes, 0, RpcServer.GRPC_MAX_SIZE)) != -1) {
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
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteFile(Metadata request, StreamObserver<OperationStatus> responseObserver) {
        responseObserver.onError(new UnsupportedOperationException("Not implemented"));
        // TODO delete the file
    }

    @Override
    public void getLeader(Subscription request, StreamObserver<Leader> responseObserver) {
        controller.addClient(responseObserver);
    }
}
