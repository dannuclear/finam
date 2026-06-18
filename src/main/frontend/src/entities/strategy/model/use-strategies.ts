import { rqClient } from "@shared/api/instance"
import type { PageType } from "@shared/api/schema"

export const useStrategies = ({
    page = 0,
    size = 20,
    sort,
    q }: PageType & { q?: string }) => {
    return rqClient.useQuery(
        "get",
        "/api/v1/strategies",
        {
            params: {
                query: {
                    page: page,
                    size: size,
                    sort: sort,
                    q: q
                }
            }
        },
        {
            staleTime: Infinity,
        })
}