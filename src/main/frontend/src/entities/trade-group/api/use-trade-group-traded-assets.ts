import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";
import { addItem, deleteItem, updateItem, withTempId, type WithChangeStatus } from "@shared/lib/change-status.lib";
import { useQuery } from "@tanstack/react-query";

export const useTradeGroupTradedAssets = (id?: number) => {

    const queryOptions = rqClient.queryOptions(
        'get',
        '/api/v1/trade-groups/{id}/traded-assets',
        {
            params: {
                path: {
                    id: id as number
                }
            }
        },
        {
            enabled: Boolean(id)
        }
    );

    const queryResult = useQuery({
        queryKey: queryOptions.queryKey,
        queryFn: async (ctx) => withTempId(await queryOptions.queryFn(ctx)),
        enabled: Boolean(id),
        placeholderData: []
    });

    const addItemToCache = (item: Partial<WithChangeStatus>) => {
        queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => addItem(prev ?? [], item))
    }

    const updateItemInCache = (item: WithChangeStatus) => {
        queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => updateItem(prev ?? [], item))
    }

    const deleteItemInCache = (_tempId: string) => {
        queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => deleteItem(prev ?? [], _tempId))
    }

    const getItemFromCache = (_tempId: string) => {
        return queryClient.getQueryData<WithChangeStatus[]>(queryOptions.queryKey)?.find(item => item._tempId === _tempId)
    }

    return {
        queryKey: queryOptions.queryKey,
        addItemToCache,
        updateItemInCache,
        deleteItemInCache,
        getItemFromCache,
        ...queryResult,
    };
}