import { rqClient } from "@shared/api/instance"

export const useTokenDetails = () => {
    return rqClient.useSuspenseQuery(
        "get",
        "/api/v1/finam/token-details",
        { staleTime: 1000 * 60 * 50 }
    )
}