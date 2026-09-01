import { rqClient } from "@shared/api/instance"
import { queryClient } from "@shared/api/query-client"

export const useOptimize = () => {
    return rqClient.useMutation('post', '/api/v1/strategies/{id}/optimize', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/analysis"))
        },
        retry: false,
    })
}