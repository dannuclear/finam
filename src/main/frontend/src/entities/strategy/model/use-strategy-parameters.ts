import { rqClient } from "@shared/api/instance"

export const useStrategyParametes = (id?: number | null) => {
    return rqClient.useQuery(
        "get",
        "/api/v1/strategies/{id}/parameters",
        {
            params: {
                path: {
                    id: id as number
                }
            }
        },
        {
            enabled: Boolean(id),
            retry: false
        }
    )
}