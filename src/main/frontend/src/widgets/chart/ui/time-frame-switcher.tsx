import { Button, ButtonGroup, type ButtonGroupProps } from '@mui/material'
import type { TimeFrameConfig } from '@shared/model/timeframes'

export interface TimeFrameSwitcherProps extends Omit<ButtonGroupProps, 'onChange'> {
  timeFrame: TimeFrameConfig | null
  options: TimeFrameConfig[],
  onChange?: (tf: TimeFrameConfig) => void
}

const TimeFrameSwitcher = ({ timeFrame, options, onChange, ...props }: TimeFrameSwitcherProps) => {
  return (
    <ButtonGroup
      variant='outlined'
      sx={{ marginBottom: 2, width: '100%', justifyContent: 'center' }}
      {...props}
    >
      {options.map(tf => (
        < Button
          key={tf.value}
          onClick={() => onChange && onChange(tf)}
          variant={tf.value === timeFrame?.value ? 'contained' : 'outlined'} >
          {tf.label}
        </Button>
      ))}
    </ButtonGroup>
  )
}

export default TimeFrameSwitcher