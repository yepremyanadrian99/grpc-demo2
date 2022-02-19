package am.adrian.grpcdemo2.client.responseobserver;

import am.adrian.grpcdemo2.model.Money;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public record MoneyResponseObserver(
        CountDownLatch countDownLatch) implements StreamObserver<Money> {

    @Override
    public void onNext(Money money) {
        System.out.println("Received asynchronously: " + money.getAmount());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is finished");
        countDownLatch.countDown();
    }
}
