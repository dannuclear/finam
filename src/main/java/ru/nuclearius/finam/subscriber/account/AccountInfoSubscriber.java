package ru.nuclearius.finam.subscriber.account;

import java.util.List;

import org.springframework.stereotype.Component;

import grpc.tradeapi.v1.accounts.AccountsServiceGrpc.AccountsServiceStub;
import grpc.tradeapi.v1.accounts.GetAccountRequest;
import grpc.tradeapi.v1.accounts.GetAccountResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.nuclearius.finam.client.dto.Account;
import ru.nuclearius.finam.client.dto.Account.Position;
import ru.nuclearius.finam.service.mapper.ProtoMapper;
import ru.nuclearius.finam.subscriber.AbstractBackoffObserver;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class AccountInfoSubscriber extends AbstractBackoffObserver<GetAccountRequest, GetAccountResponse> {
    private final AccountsServiceStub accountService;
    private String accountId = "2029595";
    private final ProtoMapper protoMapper;
    private volatile List<Position> positions = List.of();

    @Override
    public void onNext(GetAccountResponse response) {
        Account account = protoMapper.toDomain(response);
        positions = List.copyOf(account.getPositions());
    }

    @Override
    protected void subscribe() {
        if (accountId == null || accountId.isEmpty())
            return;
        GetAccountRequest request = GetAccountRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        accountService.subscribeAccount(request, this);
    }
}
