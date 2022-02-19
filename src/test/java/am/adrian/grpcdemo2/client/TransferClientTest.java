package am.adrian.grpcdemo2.client;

import am.adrian.grpcdemo2.client.streamobserver.TransferResponseObserver;
import am.adrian.grpcdemo2.model.TransferRequest;
import am.adrian.grpcdemo2.model.TransferServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransferClientTest {

    private TransferServiceGrpc.TransferServiceStub asyncStub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();
        this.asyncStub = TransferServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void testBiDirectionalTransfers() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        TransferResponseObserver responseObserver = new TransferResponseObserver(countDownLatch);
        StreamObserver<TransferRequest> requestObserver = asyncStub.transfer(responseObserver);
        for (int i = 0; i < 10; ++i) {
            String from = String.valueOf(ThreadLocalRandom.current().nextInt(1, 11));
            String to = String.valueOf(ThreadLocalRandom.current().nextInt(1, 11));
            int amount = ThreadLocalRandom.current().nextInt(1, 100);
            System.out.printf("Transferring from: %s to: %s amount: %d\n", from, to, amount);
            TransferRequest request = TransferRequest.newBuilder()
                    .setFromAccount(from)
                    .setToAccount(to)
                    .setAmount(amount)
                    .build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
