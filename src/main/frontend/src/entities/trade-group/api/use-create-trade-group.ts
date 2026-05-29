import { rqClient } from "@shared/api/instance";

export const useCreateTradeGroup = () => rqClient.useMutation('post', '/api/v1/trade-groups')