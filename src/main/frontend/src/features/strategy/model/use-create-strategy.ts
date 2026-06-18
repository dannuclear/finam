import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";

export const useCreateStrategy = () => {
    return rqClient.useMutation('post', '/api/v1/strategies', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/strategies"))
        }
    })
}