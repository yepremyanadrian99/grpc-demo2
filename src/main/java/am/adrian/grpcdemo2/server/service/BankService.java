package am.adrian.grpcdemo2.server.service;

import am.adrian.grpcdemo2.model.Balance;
import am.adrian.grpcdemo2.model.BalanceCheckRequest;
import am.adrian.grpcdemo2.model.BankServiceGrpc;
import am.adrian.grpcdemo2.model.Money;
import am.adrian.grpcdemo2.model.WithdrawRequest;
import am.adrian.grpcdemo2.server.database.AccountDatabase;
import am.adrian.grpcdemo2.server.streamobserver.DepositRequestObserver;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private final AccountDatabase database;

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        String accountNumber = request.getAccountNumber();
        System.out.printf("Received getBalance request for account: %s\n", accountNumber);
        Balance balance = Balance.newBuilder()
                .setAmount(database.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        String accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        int balance = database.getBalance(accountNumber);

        if (balance < amount) {
            System.out.println("Can't withdraw, insufficient funds");
            Status status = Status.FAILED_PRECONDITION.withDescription(
                    "Can't withdraw, insufficient funds. You only have" + balance
            );
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        System.out.printf(
                "Received withdraw request for account: %s and amount: %d\n",
                accountNumber,
                amount
        );
        // Withdraw the amount with 10$ bills.
        for (int i = 0; i < (amount / 10); ++i) {
            withdrawMoneyAndSendResponse(responseObserver, accountNumber, 10);
        }
        // If there's a remainder
        int remainder = amount % 10;
        if (remainder != 0) {
            withdrawMoneyAndSendResponse(responseObserver, accountNumber, remainder);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<am.adrian.grpcdemo2.model.DepositRequest> deposit(StreamObserver<Balance> responseObserver) {
        return new DepositRequestObserver(database, responseObserver);
    }

    private void withdrawMoneyAndSendResponse(StreamObserver<Money> responseObserver,
                                              String accountNumber, int amount) {
        database.decreaseBalance(accountNumber, amount);
        responseObserver.onNext(buildMoney(amount));
    }

    private Money buildMoney(int amount) {
        return Money.newBuilder()
                .setAmount(amount)
                .build();
    }
}
