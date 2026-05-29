import { rqClient } from "@shared/api/instance"
import type { Dayjs } from "dayjs"

const useAccountTransactions = ({
    accountId,
    startTime, 
    endTime
}: { accountId?: string, startTime?: Dayjs | null, endTime?: Dayjs | null }) => {
    const { data, ...other } = rqClient.useQuery(
        "get",
        "/api/v1/finam/accounts/{accountId}/transactions",
        {
            params: {
                path: {
                    accountId: accountId as string
                },
                query: {
                    startTime: startTime?.format(),
                    endTime: endTime?.format()
                }
            }
        },
        {
            enabled: Boolean(accountId),
            staleTime: 1000 * 60 * 50
        }
    )
    return ({
        transactionsData: data,
        ...other
    })
}

export default useAccountTransactions