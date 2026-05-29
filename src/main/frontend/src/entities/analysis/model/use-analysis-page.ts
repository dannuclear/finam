import { rqClient } from '@shared/api/instance'

export const useAnalysisPage = ({
    page = 0,
    size = 10,
    q }: { page?: number, size?: number, q?: string }) => {
    return rqClient.useQuery("get", "/api/v1/analysis", {
        params: {
            query: {
                page,
                size,
                q
            }
        }
    })
}