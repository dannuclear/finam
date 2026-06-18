import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";
import type { StrategyAsset } from "@shared/api/schema";
import { addItem, deleteItem, updateItem, withTempId, type WithChangeStatus } from "@shared/lib/change-status.lib";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

export const useStrategyAssets = (id?: number) => {
    const [strategyAsset, setStrategyAsset] = useState<StrategyAsset | null>(null)

    const queryOptions = rqClient.queryOptions(
        'get',
        '/api/v1/strategies/{id}/assets',
        {
            params: {
                path: {
                    id: id as number
                }
            }
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
        setStrategyAsset(null)
    }

    const updateItemInCache = (item: WithChangeStatus) => {
        if (item)
            queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => updateItem(prev ?? [], item))
        setStrategyAsset(null)
    }

    const deleteItemInCache = (_tempId: string) => {
        queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => deleteItem(prev ?? [], _tempId))
    }

    const getItemFromCache = (_tempId: string) => {
        return queryClient.getQueryData<WithChangeStatus[]>(queryOptions.queryKey)?.find(item => item._tempId === _tempId)
    }

    const prepare = (values?: StrategyAsset) => {
        setStrategyAsset(values ?? {})
    }

    const edit = (_tempId: string) => {
        const item = getItemFromCache(_tempId)
        if (item)
            setStrategyAsset(item)
    }

    const cancel = () => {
        setStrategyAsset(null)
    }

    const resetCache = () => {
        queryClient.invalidateQueries(queryOptions)
    }

    return {
        queryKey: queryOptions.queryKey,
        addItemToCache,
        updateItemInCache,
        deleteItemInCache,
        getItemFromCache,
        prepare,
        strategyAsset,
        edit,
        cancel,
        resetCache,
        ...queryResult,
    };
}