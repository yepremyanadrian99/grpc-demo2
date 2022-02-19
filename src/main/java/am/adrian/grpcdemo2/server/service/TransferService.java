package am.adrian.grpcdemo2.server.service;

import am.adrian.grpcdemo2.model.TransferRequest;
import am.adrian.grpcdemo2.model.TransferResponse;
import am.adrian.grpcdemo2.model.TransferServiceGrpc;
import am.adrian.grpcdemo2.server.database.AccountDatabase;
import am.adrian.grpcdemo2.server.streamobserver.TransferRequestObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    private final AccountDatabase database;

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferRequestObserver(responseObserver, database);
    }
}
