import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";

export const useUpdateStrategy = () => {
    return rqClient.useMutation('put', '/api/v1/strategies/{id}', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/strategies"))
        }
    })
}