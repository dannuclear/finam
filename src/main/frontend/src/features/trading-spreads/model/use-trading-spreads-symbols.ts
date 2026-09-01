import { rqClient } from "@shared/api/instance";

export const useTradingSpreadsSymbols = () => rqClient.useQuery('get', '/api/v1/spreads/symbols')