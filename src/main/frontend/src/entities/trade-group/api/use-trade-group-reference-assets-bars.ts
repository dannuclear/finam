import { rqClient } from "@shared/api/instance";
import type { TimeFrame } from "@shared/api/schema";
import type { Dayjs } from "dayjs";

export const useTradeGroupReferenceAssetsBars = ({
    id,
    timeFrame,
    startTime,
    endTime,
    smaBarCount = 30
}: {
    id: number | null,
    timeFrame: TimeFrame,
    startTime: Dayjs
    endTime: Dayjs,
    smaBarCount?: number
}) => {
    return rqClient.useQuery(
        'get',
        '/api/v1/trade-groups/{id}/reference-assets/bars',
        {
            params: {
                path: {
                    id: id as number
                },
                query: {
                    timeFrame: timeFrame,
                    startTime: startTime.format(),
                    endTime: endTime.format(),
                    smaBarCount: smaBarCount
                }
            }
        },
        {
            enabled: Boolean(id),
            staleTime: 1000 * 60 * 50,
            retry: false,
            placeholderData: prev => prev
        }
    );
}