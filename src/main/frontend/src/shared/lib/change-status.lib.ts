import { nanoid } from 'nanoid'
export type ChangeStatus = "NEW" | "UPDATED" | "DELETED" | undefined;

export type WithChangeStatus = {
    _tempId?: string;
    changeStatus?: ChangeStatus;
}

const isSame = <T extends WithChangeStatus>(a: T, b: T) =>
    a._tempId === b._tempId;

export const withTempId = <T>(data: T[]): (T & WithChangeStatus)[] =>
    data.map(item => ({
        ...item,
        _tempId: nanoid()
    }));

/** Добавление нового элемента */
export const addItem = <T extends WithChangeStatus>(
    list: T[],
    item: WithChangeStatus
): T[] => [
        ...list,
        {
            ...item,
            _tempId: nanoid(),
            changeStatus: "NEW"
        } as T,
    ];

/** Обновление существующего элемента */
export const updateItem = <T extends WithChangeStatus>(
    list: T[],
    updated: T
): T[] =>
    list.map(item => {
        if (!isSame(item, updated)) return item;

        if (item.changeStatus === "NEW") {
            return {
                ...item,
                ...updated,
                _tempId: item._tempId,
                changeStatus: "NEW"
            };
        }

        return {
            ...item,
            ...updated,
            changeStatus: "UPDATED"
        };
    });

export const deleteItem = <T extends WithChangeStatus>(list: T[], _tempId: string): T[] =>
    list.flatMap(item => item._tempId !== _tempId ? [item] : item.changeStatus === "NEW" ? [] : [{ ...item, changeStatus: "DELETED" }])
