import { rqClient } from '@shared/api/instance'

const useAssetPage = ({
    page = 0,
    size = 10,
    q }: { page?: number, size?: number, q?: string }) => {
    return rqClient.useQuery("get", "/api/v1/finam/assets", {
        params: {
            query: {
                page,
                size,
                q
            }
        }
    })
}

export default useAssetPage