import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";
import type { AnalysisAsset } from "@shared/api/schema";
import { addItem, deleteItem, updateItem, withTempId, type WithChangeStatus } from "@shared/lib/change-status.lib";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

export const useAnalysisAssets = (id?: number) => {
    const [analysisAsset, setAnalysisAsset] = useState<AnalysisAsset | null>(null)

    const queryOptions = rqClient.queryOptions(
        'get',
        '/api/v1/analysis/{id}/assets',
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
        setAnalysisAsset(null)
    }

    const updateItemInCache = (item: WithChangeStatus) => {
        if (item)
            queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => updateItem(prev ?? [], item))
        setAnalysisAsset(null)
    }

    const deleteItemInCache = (_tempId: string) => {
        queryClient.setQueryData(queryOptions.queryKey, (prev: WithChangeStatus[]) => deleteItem(prev ?? [], _tempId))
    }

    const getItemFromCache = (_tempId: string) => {
        return queryClient.getQueryData<WithChangeStatus[]>(queryOptions.queryKey)?.find(item => item._tempId === _tempId)
    }

    const prepare = (values?: AnalysisAsset) => {
        setAnalysisAsset(values ?? {})
    }

    const edit = (_tempId: string) => {
        const item = getItemFromCache(_tempId)
        if (item)
            setAnalysisAsset(item)
    }

    const cancel = () => {
        setAnalysisAsset(null)
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
        analysisAsset,
        edit,
        cancel,
        resetCache,
        ...queryResult,
    };
}