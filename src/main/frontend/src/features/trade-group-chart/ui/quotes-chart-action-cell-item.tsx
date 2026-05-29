import type { GridActionsCellItemProps } from '@mui/x-data-grid'
import { GridActionsCellItem } from '@mui/x-data-grid'
import { DefaultIcon } from '@shared/ui'

type QuoteChartActionCellItemProps = Omit<GridActionsCellItemProps, "icon" | "label" | "color" | "size" | 'material'>

const QuoteChartActionCellItem = ({ ...props }: QuoteChartActionCellItemProps) => {
    return (
        <GridActionsCellItem
            icon={<DefaultIcon iconName="fa-chart-line-up-down" />}
            label="Котировки"
            color="primary"
            size="medium"
            {...props}
        />
    )
}

export default QuoteChartActionCellItem