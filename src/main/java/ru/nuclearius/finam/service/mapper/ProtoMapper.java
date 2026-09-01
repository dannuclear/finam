package ru.nuclearius.finam.service.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import grpc.tradeapi.v1.accounts.GetAccountResponse;
import grpc.tradeapi.v1.accounts.TradesResponse;
import grpc.tradeapi.v1.accounts.TransactionsResponse;
import grpc.tradeapi.v1.assets.AllAssetsResponse;
import grpc.tradeapi.v1.assets.GetAssetResponse;
import grpc.tradeapi.v1.auth.TokenDetailsResponse;
import grpc.tradeapi.v1.marketdata.BarsResponse;
import grpc.tradeapi.v1.marketdata.SubscribeQuoteResponse;
import grpc.tradeapi.v1.orders.OrderType;
import ru.nuclearius.finam.client.dto.Account;
import ru.nuclearius.finam.client.dto.AllAssets;
import ru.nuclearius.finam.client.dto.AllBars;
import ru.nuclearius.finam.client.dto.AllQuotes;
import ru.nuclearius.finam.client.dto.AssetInfo;
import ru.nuclearius.finam.client.dto.TokenDetails;
import ru.nuclearius.finam.client.dto.TradeHistory;
import ru.nuclearius.finam.client.dto.TransactionList;
import ru.nuclearius.finam.service.domain.Order;
import ru.nuclearius.finam.service.domain.Order.Side;
import ru.nuclearius.finam.service.domain.Order.StopCondition;
import ru.nuclearius.finam.service.domain.Order.TimeInForce;
import ru.nuclearius.finam.service.domain.Order.Type;
import ru.nuclearius.finam.service.domain.OrderState;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProtoMapper extends ProtobufStandardMappings {
    @Mapping(target = "accountIds", source = "accountIdsList")
    @Mapping(target = "mdPermissions", source = "mdPermissionsList")
    TokenDetails toDomain(TokenDetailsResponse proto);

    @Mapping(target = "cash", source = "cashList")
    @Mapping(target = "positions", source = "positionsList")
    Account toDomain(GetAccountResponse proto);

    @Mapping(target = "trades", source = "tradesList")
    TradeHistory toDomain(TradesResponse proto);

    @Mapping(target = "transactions", source = "transactionsList")
    TransactionList toDomain(TransactionsResponse proto);

    @Mapping(target = "assets", source = "assetsList")
    AllAssets toDomain(AllAssetsResponse proto);

    @Mapping(target = "bars", source = "barsList")
    AllBars toDomain(BarsResponse proto);

    @Mapping(target = "quotes", source = "quoteList")
    AllQuotes toDomain(SubscribeQuoteResponse proto);

    AssetInfo toDomain(GetAssetResponse proto);

    Order toDomain(grpc.tradeapi.v1.orders.Order proto);

    grpc.tradeapi.v1.orders.Order map(Order domain);

    OrderState map(grpc.tradeapi.v1.orders.OrderState proto);
    grpc.tradeapi.v1.orders.OrderState map(OrderState proto);

    Side map (grpc.tradeapi.v1.Side proto);
    grpc.tradeapi.v1.Side map (Side domain);

    Type map (OrderType proto);
    OrderType map (Type domain);

    TimeInForce map (grpc.tradeapi.v1.orders.TimeInForce proto);
    grpc.tradeapi.v1.orders.TimeInForce map (TimeInForce domain);
    
    StopCondition map (grpc.tradeapi.v1.orders.StopCondition proto);
    grpc.tradeapi.v1.orders.StopCondition map (StopCondition domain);
}
