import type { WithChangeStatus } from "@shared/lib/change-status.lib"
import type { components, operations, paths } from "./generated"

export type ApiPaths = paths
export type ApiComponents = components

export type SearchResultDTO = { objectid: number, fullName: string, objectguid: string, level: number }

// export type Meta = ApiComponents["schemas"]["Meta"]
export type Account = ApiComponents["schemas"]["Account"]
export type TradeHistory = ApiComponents["schemas"]["TradeHistory"]
export type Transaction = ApiComponents["schemas"]["Transaction"]
export type TimeFrame = operations["bars"]["parameters"]["query"]["timeFrame"]
export type TradeGroup = ApiComponents["schemas"]["TradeGroupDTO"]
export type TradeGroupTradedAsset = ApiComponents["schemas"]["TradeGroupTradedAssetDTO"] & Partial<WithChangeStatus>
export type TradeGroupReferenceAsset = ApiComponents["schemas"]["TradeGroupReferenceAssetDTO"] & Partial<WithChangeStatus>
export type Asset = ApiComponents["schemas"]["Asset"]
export type Series = ApiComponents["schemas"]["Series"]
export type Analysis = ApiComponents["schemas"]["Analysis"]
export type Strategy = ApiComponents["schemas"]["Strategy"]
export type StrategyAsset = ApiComponents["schemas"]["StrategyAsset"] & Partial<WithChangeStatus>
export type AnalysisAsset = ApiComponents["schemas"]["AnalysisAsset"] & Partial<WithChangeStatus>


// export type ItemWithMeta = { meta?: Meta }

export type PageType = {
    page?: number,
    size?: number,
    sort?: string[],
}

export type ApiPathsWithoutParams = {
    [K in keyof ApiPaths]: K extends `${string}{${string}}${string}` ? never : K;
}[keyof ApiPaths];

export type NumberOrNew = number | 'new'

type ExtractPathsWithPagination<T extends Record<string, any>> = {
    [Path in keyof T as T[Path] extends {
        get: {
            parameters: {
                query?: infer Query;
            };
        };
    } ? (Query extends { page?: number, size?: number } ? Path : never) : never]: T[Path];
};

export type PaginatedPaths = ExtractPathsWithPagination<ApiPaths>;