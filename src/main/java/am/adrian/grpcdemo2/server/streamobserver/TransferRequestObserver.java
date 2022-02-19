package am.adrian.grpcdemo2.server.streamobserver;

import am.adrian.grpcdemo2.model.Account;
import am.adrian.grpcdemo2.model.TransferResponse;
import am.adrian.grpcdemo2.model.TransferStatus;
import am.adrian.grpcdemo2.server.database.AccountDatabase;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ThreadLocalRandom;

public record TransferRequestObserver(
        StreamObserver<TransferResponse> responseStreamObserver,
        AccountDatabase accountDatabase)
        implements StreamObserver<am.adrian.grpcdemo2.model.TransferRequest> {

    @Override
    public void onNext(am.adrian.grpcdemo2.model.TransferRequest request) {
        String from = request.getFromAccount();
        String to = request.getToAccount();
        int amount = request.getAmount();
        Integer balance = accountDatabase.getBalance(from);
        TransferStatus status = TransferStatus.FAILED;
        if (!from.equals(to) && balance >= amount) {
            accountDatabase.decreaseBalance(from, amount);
            accountDatabase.increaseBalance(to, amount);
            status = TransferStatus.SUCCESSFUL;
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Account.Builder fromAccount = Account.newBuilder()
                .setAccountNumber(from)
                .setBalance(accountDatabase.getBalance(from));
        Account.Builder toAccount = Account.newBuilder()
                .setAccountNumber(to)
                .setBalance(accountDatabase.getBalance(to));
        TransferResponse transferResponse = TransferResponse.newBuilder()
                .addAccounts(fromAccount)
                .addAccounts(toAccount)
                .setStatus(status)
                .build();
        responseStreamObserver.onNext(transferResponse);
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onCompleted() {
        accountDatabase.printDatabase();
        responseStreamObserver.onCompleted();
    }
}
