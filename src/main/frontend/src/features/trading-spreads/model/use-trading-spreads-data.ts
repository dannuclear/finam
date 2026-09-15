import { rqClient } from "@shared/api/instance";

export const useTradingSpreadsData = () => rqClient.useQuery('get', '/api/v1/spreads/data', {}, { refetchOnWindowFocus: false, retry: false })