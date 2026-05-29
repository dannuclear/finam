import { rqClient } from "@shared/api/instance"
import type { TimeFrame } from "@shared/api/schema"
import type { Dayjs } from "dayjs"

const useBars = ({
    symbols,
    timeFrame,
    startTime,
    endTime
}: {
    symbols: string[] | null,
    timeFrame: TimeFrame,
    startTime: Dayjs
    endTime: Dayjs
}) => {    
    return rqClient.useQuery(
        "get",
        "/api/v1/finam/assets/bars",
        {
            params: {
                query: {
                    symbols: symbols ?? [],
                    timeFrame: timeFrame,
                    startTime: startTime.format(),
                    endTime: endTime.format(),
                }
            }
        },
        {
            enabled: symbols !== null && symbols.length > 0,
            staleTime: 1000 * 60 * 50,
            retry: false
        }
    )
}

export default useBars