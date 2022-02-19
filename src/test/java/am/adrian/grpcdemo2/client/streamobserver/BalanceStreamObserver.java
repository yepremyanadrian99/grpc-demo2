package am.adrian.grpcdemo2.client.streamobserver;

import am.adrian.grpcdemo2.model.Balance;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public record BalanceStreamObserver(
        CountDownLatch countDownLatch) implements StreamObserver<Balance> {

    @Override
    public void onNext(Balance balance) {
        System.out.println("Final balance is: " + balance.getAmount());
    }

    @Override
    public void onError(Throwable throwable) {
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is finished");
        countDownLatch.countDown();
    }
}
