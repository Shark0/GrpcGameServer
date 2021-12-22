package com.shark.game;

import com.shark.game.manager.SceneManager;
import com.shark.game.service.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws IOException, InterruptedException {
        SceneManager.getInstance().init();
        Server server = ServerBuilder.forPort(8080)
                .addService(new LoginServiceImpl())
                .addService(new TexasHoldemGameStatusServiceImpl())
                .addService(new TexasHoldemGameOperationServiceImpl())
                .build();
        server.start();
        System.out.println("服務啟動");
        server.awaitTermination();
    }
}
