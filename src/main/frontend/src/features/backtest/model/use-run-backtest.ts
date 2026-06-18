import { rqClient } from "@shared/api/instance"
import { queryClient } from "@shared/api/query-client"

export const useRunBacktest = () => {
    return rqClient.useMutation('post', '/api/v1/strategies/{id}/backtest', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/analysis"))
        }
    })
}