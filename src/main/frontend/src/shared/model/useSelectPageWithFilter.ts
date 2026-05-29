
import { rqClient } from '@shared/api/instance';
import type { PageType, PaginatedPaths } from '@shared/api/schema';
import { keepPreviousData } from '@tanstack/react-query';

export const useSelectPageWithFilter = (
    path: keyof PaginatedPaths,
    { page = 0, size, sort }: PageType,
    q?: string,
    filters?: PaginatedPaths[typeof path]["get"]["parameters"]["query"],
    enabled: boolean = true,
    placeholderData: object = keepPreviousData,
    staleTime: number = 0
) => {
    return rqClient.useQuery("get", path, {
        params: {
            query: {
                page,
                size,
                sort,
                q,
                ...filters
            }
        }
    }, { enabled, placeholderData, staleTime: staleTime })
}