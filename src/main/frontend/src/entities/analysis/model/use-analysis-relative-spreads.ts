import { rqClient } from '@shared/api/instance'
import type { TimeFrame } from '@shared/api/schema'
import type { Dayjs } from 'dayjs'

export const useAnalysisRelativeSpreads = ({
    analysisId,
    timeFrame,
    startTime,
    endTime,
    averageTimeFrame,
    averageStartTime,
    averageEndTime
}: {
    analysisId?: number,
    timeFrame: TimeFrame,
    startTime: Dayjs,
    endTime: Dayjs,
    averageTimeFrame: TimeFrame,
    averageStartTime: Dayjs,
    averageEndTime: Dayjs,
}) => {
    return rqClient.useQuery("get", "/api/v1/analysis/{id}/relative-spreads", {
        params: {
            path: {
                id: analysisId!
            },
            query: {
                timeFrame,
                startTime: startTime.format(),
                endTime: endTime.format(),
                averageTimeFrame,
                averageStartTime: averageStartTime.format(),
                averageEndTime: averageEndTime.format()
            }
        }
    }, {
        enabled: analysisId != null,
        refetchOnWindowFocus: false
    })
}