package ru.nuclearius.finam.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer;
import org.springframework.grpc.client.ImportGrpcClients;

import grpc.tradeapi.v1.accounts.AccountsServiceGrpc.AccountsServiceBlockingStub;
import grpc.tradeapi.v1.assets.AssetsServiceGrpc.AssetsServiceBlockingStub;
import grpc.tradeapi.v1.auth.AuthServiceGrpc.AuthServiceBlockingStub;
import grpc.tradeapi.v1.auth.AuthServiceGrpc.AuthServiceStub;
import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc.MarketDataServiceBlockingStub;
import grpc.tradeapi.v1.marketdata.MarketDataServiceGrpc.MarketDataServiceStub;
import grpc.tradeapi.v1.orders.OrdersServiceGrpc.OrdersServiceBlockingStub;
import grpc.tradeapi.v1.orders.OrdersServiceGrpc.OrdersServiceStub;

@Configuration
@ImportGrpcClients(types = {
        AccountsServiceBlockingStub.class,
        AssetsServiceBlockingStub.class,
        AuthServiceBlockingStub.class,
        MarketDataServiceStub.class,
        MarketDataServiceBlockingStub.class,
        OrdersServiceStub.class,
        OrdersServiceBlockingStub.class
})
@ImportGrpcClients(types = { AuthServiceStub.class }, target = "auth")
public class FinamConfig {

    @Bean
    GrpcChannelBuilderCustomizer<?> stubs(JwtTokenHolder tokenHolder) {
        DynamicBearerTokenInterceptor interceptor = new DynamicBearerTokenInterceptor(tokenHolder);
        return GrpcChannelBuilderCustomizer.matching("default", builder -> builder.intercept(interceptor));
    }
}
