package ru.nuclearius.finam.subscriber.token;

import org.springframework.core.env.Environment;

import grpc.tradeapi.v1.auth.AuthServiceGrpc.AuthServiceStub;
import grpc.tradeapi.v1.auth.SubscribeJwtRenewalRequest;
import grpc.tradeapi.v1.auth.SubscribeJwtRenewalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.grpc.JwtTokenHolder;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;

@Slf4j
@RequiredArgsConstructor
public class JwtRenewalSubscriber
        extends AbstractBackoffObserver<SubscribeJwtRenewalRequest, SubscribeJwtRenewalResponse> {

    private final AuthServiceStub authServiceStub;
    private final Environment environment;
    private final JwtTokenHolder tokenHolder;

    @Override
    protected void subscribe() {
        String secret = environment.getRequiredProperty("finam.rest.secret");

        SubscribeJwtRenewalRequest request = SubscribeJwtRenewalRequest.newBuilder()
                .setSecret(secret)
                .build();

        authServiceStub.subscribeJwtRenewal(request, this);
    }

    @Override
    public void onNext(SubscribeJwtRenewalResponse response) {
        log.info("JWT token renewed");
        tokenHolder.setToken(response.getToken());
    }
}