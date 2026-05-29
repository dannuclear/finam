import { GridActionsCellItem, type GridActionsCellItemProps, type GridActionsColDef, type GridRowId, type GridRowParams } from '@mui/x-data-grid';
import type { GridBaseColDef } from '@mui/x-data-grid/internals';
import { DefaultIcon } from '../DefaultIcon';

export interface DefaultGridActionColDef extends Omit<GridBaseColDef, 'field'> {
  onEdit?: (id: GridRowId) => void,
  onDelete?: (id: GridRowId) => void,
  extraActions?: (params: GridRowParams) => React.ReactElement<GridActionsCellItemProps>[]
}

export const DefaultGridActionColumn = ({ onEdit, onDelete, extraActions, ...props }: DefaultGridActionColDef): GridActionsColDef => {

  const onDeleteInternal = (id: GridRowId) => {
    if (!confirm("Вы действительно хотите удалить?"))
      return;
    if (onDelete) onDelete(id);
  }
  
  return (
    {
      ...props,
      field: 'actions',
      type: 'actions',
      width: 150,
      getActions: (params) => [
        ...(extraActions ? extraActions(params) : []),
        onEdit && <GridActionsCellItem icon={<DefaultIcon iconName='fa-pencil' />} label='Изменить' onClick={() => onEdit(params.id)} color='primary' size='medium' />,
        onDelete && <GridActionsCellItem icon={<DefaultIcon iconName='fa-trash' />} label='Удалить' onClick={() => onDeleteInternal(params.id)} color='primary' size='medium' />,
      ].filter(el => !!el)
    }
  )
}
