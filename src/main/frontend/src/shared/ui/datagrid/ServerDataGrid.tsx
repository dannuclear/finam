import type { GridFilterModel, GridPaginationModel, GridRowId } from '@mui/x-data-grid';
import type { PaginatedPaths } from '@shared/api/schema';
import { keepPreviousData } from '@tanstack/react-query';
import { useState } from 'react';
import { BaseDataGrid, type BaseDataGridProps } from './BaseDataGrid';
import { useSelectPageWithFilter } from '@shared/model/useSelectPageWithFilter';

export interface ServerDataGridProps<P extends keyof PaginatedPaths = keyof PaginatedPaths> extends Omit<BaseDataGridProps, 'rows'> {
    path: P,
    extraParams?: object,
    onAdd?: () => void,
    onEdit?: (id: GridRowId) => void,
    onDelete?: (id: GridRowId) => void,
    filters?: PaginatedPaths[P]["get"]["parameters"]["query"]
}

export const ServerDataGrid = <P extends keyof PaginatedPaths>({ path, filters, ...props }: ServerDataGridProps<P>) => {
    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 })
    const [filterModel, setFilterModel] = useState<GridFilterModel>({ items: [] })
    const { data, isFetching } = useSelectPageWithFilter(
        path,
        {
            page: paginationModel.page,
            size: paginationModel.pageSize,
            sort: ['id']
        },
        filterModel.quickFilterValues?.length ? filterModel.quickFilterValues[0] : undefined,
        filters,
        true,
        keepPreviousData
    )

    return (
        <BaseDataGrid
            rows={data?.content ?? []}
            loading={isFetching}
            rowCount={data?.page?.totalElements ?? 0}

            // Фильтрация
            filterMode='server'
            filterModel={filterModel}
            onFilterModelChange={setFilterModel}

            // Пагинация
            paginationMode='server'
            paginationModel={paginationModel}
            onPaginationModelChange={setPaginationModel}
            pageSizeOptions={[5, 10, 20, 40, 100]}

            slotProps={{
                loadingOverlay: {
                    variant: 'linear-progress',
                    noRowsVariant: 'linear-progress',
                }
            }}
            {...props}
        />
    )
}
