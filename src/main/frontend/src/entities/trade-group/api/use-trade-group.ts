import { rqClient } from "@shared/api/instance"

export const useTradeGroup = (id?: number | null) => {
    return rqClient.useQuery(
        "get",
        "/api/v1/trade-groups/{id}",
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