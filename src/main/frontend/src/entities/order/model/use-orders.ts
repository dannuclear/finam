import { rqClient } from '@shared/api/instance';
import { queryClient } from '@shared/api/query-client';
import type { OrderState } from '@shared/api/schema';
import { useQuery } from '@tanstack/react-query';

export const useOrders = (symbol: string) => {
    const queryOptions = rqClient.queryOptions(
        'get',
        '/api/v1/finam/assets/{symbol}/orders',
        {
            params: {
                path: {
                    symbol: symbol
                }
            }
        }
    );

    const { data } = useQuery({
        queryKey: queryOptions.queryKey,
        queryFn: queryOptions.queryFn,
        enabled: symbol !== null,
        staleTime: 1000 * 60 * 50,
        retry: false
    })

    const update = (order: OrderState) => {
        queryClient.setQueryData<OrderState[]>(
            queryOptions.queryKey,
            (prev = []) => {
                const index = prev.findIndex(o => o.orderId === order.orderId);

                if (index === -1) {
                    return [...prev, order];
                }

                const next = [...prev];
                next[index] = order;
                return next;
            }
        );
    };

    const remove = (orderId: string) => {
        queryClient.setQueryData<OrderState[]>(
            queryOptions.queryKey,
            (prev = []) => prev.filter(o => o.orderId !== orderId)
        );
    };

    return {
        data: data,
        update: update,
        remove: remove
    }
}