import { rqClient } from "@shared/api/instance"

export const useStrategy = (id?: number | null) => {
    return rqClient.useQuery(
        "get",
        "/api/v1/strategies/{id}",
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