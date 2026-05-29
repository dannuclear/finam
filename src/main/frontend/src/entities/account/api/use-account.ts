import { rqClient } from "@shared/api/instance"

const useAccount = ({
    accountId
}: { accountId: string }) => {
    const { data, ...other } = rqClient.useQuery(
        "get",
        "/api/v1/finam/accounts/{accountId}",
        {
            params: {
                path: {
                    accountId
                }
            }
        },
        {
            enabled: Boolean(accountId),
            staleTime: 1000 * 60 * 50
        }
    )
    return ({
        account: data,
        ...other
    })
}

export default useAccount