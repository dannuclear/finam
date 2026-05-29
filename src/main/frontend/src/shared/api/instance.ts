import createFetchClient, { type Middleware } from "openapi-fetch";
import createClient from "openapi-react-query";
import type { ApiPaths } from "./schema";

export const fetchClient = createFetchClient<ApiPaths>({
    credentials: "include"
})

const middleware: Middleware = {
    async onResponse({ response }) {
        if (response.status === 401) {
            console.log('unauthorized');
            window.dispatchEvent(new Event("unauthorized"))
        }
    }
};

fetchClient.use(middleware)


export const rqClient = createClient(fetchClient);