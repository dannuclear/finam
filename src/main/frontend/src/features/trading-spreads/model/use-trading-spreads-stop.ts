import { rqClient } from "@shared/api/instance"
import { queryClient } from "@shared/api/query-client"

export const useTradingSpreadsStop = () => {
    return rqClient.useMutation('post', '/api/v1/spreads/stop', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/spreads/status"))
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/spreads/symbols"))
        },
        retry: false,
    })
}