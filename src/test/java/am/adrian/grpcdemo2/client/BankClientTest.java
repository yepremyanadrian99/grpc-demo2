package am.adrian.grpcdemo2.client;

import am.adrian.grpcdemo2.client.streamobserver.BalanceResponseObserver;
import am.adrian.grpcdemo2.client.streamobserver.MoneyResponseObserver;
import am.adrian.grpcdemo2.model.Balance;
import am.adrian.grpcdemo2.model.BalanceCheckRequest;
import am.adrian.grpcdemo2.model.BankServiceGrpc;
import am.adrian.grpcdemo2.model.DepositRequest;
import am.adrian.grpcdemo2.model.Money;
import am.adrian.grpcdemo2.model.WithdrawRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAccumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    private BankServiceGrpc.BankServiceStub asyncStub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.asyncStub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceCheckTest() {
        final String accountNumber = "1";
        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        Balance balance = blockingStub.getBalance(request);
        System.out.println("Received: " + balance);
    }

    @Test
    public void withdrawTest() {
        final String accountNumber = "9";
        final int amountToBeWithdrawn = 799;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        int initialBalance = blockingStub.getBalance(balanceCheckRequest).getAmount();

        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .setAmount(amountToBeWithdrawn)
                .build();

        LongAccumulator counter = new LongAccumulator(Long::sum, 0L);
        Iterator<Money> moneyIterator = blockingStub.withdraw(withdrawRequest);
        moneyIterator.forEachRemaining(money -> {
            counter.accumulate(1);
            System.out.println("Withdrew " + money.getAmount() + "$ successfully");
        });

        assertEquals(
                counter.get(),
                (amountToBeWithdrawn / 10) + (amountToBeWithdrawn % 10 & 1)
        );

        int balanceAfterWithdrawal = blockingStub.getBalance(balanceCheckRequest).getAmount();
        assertEquals(balanceAfterWithdrawal, initialBalance - amountToBeWithdrawn);
    }

    @Test
    public void withdrawAsyncTest() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final String accountNumber = "8";
        final int amountToBeWithdrawn = 145;
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .setAmount(amountToBeWithdrawn)
                .build();
        asyncStub.withdraw(withdrawRequest, new MoneyResponseObserver(countDownLatch));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
    }

    @Test
    public void depositStreamTest() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<DepositRequest> depositStreamObserver = asyncStub.deposit(
                new BalanceResponseObserver(countDownLatch)
        );
        for (int i = 0; i < 100; ++i) {
            DepositRequest depositRequest = DepositRequest.newBuilder()
                    .setAccountNumber("1")
                    .setAmount(100)
                    .build();
            depositStreamObserver.onNext(depositRequest);
        }
        depositStreamObserver.onCompleted();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
