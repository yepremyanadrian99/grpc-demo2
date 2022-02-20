package am.adrian.grpcdemo2.server.streamobserver;

import am.adrian.grpcdemo2.model.Balance;
import am.adrian.grpcdemo2.server.database.AccountDatabase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DepositRequestObserver implements StreamObserver<am.adrian.grpcdemo2.model.DepositRequest> {

    private final AccountDatabase database;

    private final StreamObserver<Balance> responseObserver;

    private int balance;

    @Override
    public void onNext(am.adrian.grpcdemo2.model.DepositRequest request) {
        String account = request.getAccountNumber();
        int amount = request.getAmount();
        balance = database.increaseBalance(account, amount);
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onCompleted() {
        Balance balanceResponse = Balance.newBuilder()
                .setAmount(balance)
                .build();
        responseObserver.onNext(balanceResponse);
        responseObserver.onCompleted();
    }
}
