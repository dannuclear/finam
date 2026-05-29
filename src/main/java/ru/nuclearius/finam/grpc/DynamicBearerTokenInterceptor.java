package ru.nuclearius.finam.grpc;

import org.springframework.grpc.server.security.GrpcSecurity;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicBearerTokenInterceptor implements ClientInterceptor {
    private final JwtTokenHolder tokenHolder;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(GrpcSecurity.AUTHORIZATION_KEY, "Bearer " + tokenHolder.getToken());
                super.start(responseListener, headers);
            }
        };
    }

}
