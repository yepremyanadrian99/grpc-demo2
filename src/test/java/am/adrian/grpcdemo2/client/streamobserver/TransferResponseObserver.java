package am.adrian.grpcdemo2.client.streamobserver;

import am.adrian.grpcdemo2.model.TransferResponse;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public record TransferResponseObserver(
        CountDownLatch countDownLatch) implements StreamObserver<TransferResponse> {

    @Override
    public void onNext(TransferResponse transferResponse) {
        System.out.println("Transfer status: " + transferResponse.getStatus());
        transferResponse.getAccountsList().stream()
                .map(account -> account.getAccountNumber() + ":" + account.getBalance())
                .forEach(System.out::println);
        System.out.println("----------------------------------------------------------");
    }

    @Override
    public void onError(Throwable t) {
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("All transfers are done");
        countDownLatch.countDown();
    }
}
