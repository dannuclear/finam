import { QueryCache, QueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";

export const queryClient = new QueryClient({
  defaultOptions: {
    mutations: {
      onError: (error) => {
        toast.error("Ошибка при запросе: " + error["message"])
      }
    },
    queries: {
      // staleTime: 10000,
      // staleTime: 5 * 60 * 1000,
      // gcTime: 5 * 60 * 1000,
    },
  },
  queryCache: new QueryCache({
    onError: (error: unknown) => {
      toast.error("Ошибка при запросе: " + (error as Error).message);
    },
  })
})