package am.adrian.grpcdemo2.server;

import am.adrian.grpcdemo2.server.config.ServerConfig;
import am.adrian.grpcdemo2.server.service.BankService;
import am.adrian.grpcdemo2.server.service.TransferService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class MainApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);

        BankService bankService = context.getBean(BankService.class);
        TransferService transferService = context.getBean(TransferService.class);

        Server server = ServerBuilder.forPort(8080)
                .addService(bankService)
                .addService(transferService)
                .build();
        server.start();
        server.awaitTermination();
    }
}
