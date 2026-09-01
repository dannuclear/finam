import { rqClient } from "@shared/api/instance";

export const useTradingSpreadsStatus = () => rqClient.useQuery('get', '/api/v1/spreads/status')