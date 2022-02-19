package am.adrian.grpcdemo2.client;

import am.adrian.grpcdemo2.model.Balance;
import am.adrian.grpcdemo2.model.BalanceCheckRequest;
import am.adrian.grpcdemo2.model.BankServiceGrpc;
import am.adrian.grpcdemo2.model.Money;
import am.adrian.grpcdemo2.model.WithdrawRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Iterator;
import java.util.concurrent.atomic.LongAccumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
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
}
