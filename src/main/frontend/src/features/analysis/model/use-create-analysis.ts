import { rqClient } from "@shared/api/instance";
import { queryClient } from "@shared/api/query-client";

export const useCreateAnalysis = () => {
    return rqClient.useMutation('post', '/api/v1/analysis', {
        onSuccess: () => {
            queryClient.invalidateQueries(rqClient.queryOptions("get", "/api/v1/analysis"))
        }
    })
}