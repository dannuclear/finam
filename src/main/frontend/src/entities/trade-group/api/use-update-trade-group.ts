import { rqClient } from "@shared/api/instance";

export const useUpdateTradeGroup = () => rqClient.useMutation('put', '/api/v1/trade-groups/path/{id}')