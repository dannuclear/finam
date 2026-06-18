import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";

export const useDeleteStrategy = () => {
    return rqClient.useMutation('delete', '/api/v1/strategies/{id}', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/strategies"))
        }
    })
}