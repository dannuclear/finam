import '@assets/css/all.min.css';
import { ruRU } from '@mui/material/locale';
import { createTheme } from '@mui/material/styles';
import type { } from '@mui/x-data-grid/themeAugmentation';
// import type { } from '@mui/x-tree-view/themeAugmentation';
import type { } from '@mui/x-date-pickers/themeAugmentation';

export const theme = createTheme({
    palette: {
        mode: 'light'
    },
    components: {
        MuiTextField: {
            defaultProps: {
                size: 'small',
                fullWidth: true,
                autoComplete: "off"
            }
        },
        MuiSelect: {
            defaultProps: {
                size: 'small',
            }
        },
        MuiButton: {
            defaultProps: {
                variant: 'outlined',
                size: 'small'
            }
        },
        MuiDatePicker: {
            defaultProps: {
                slotProps: {
                    textField: {
                        size: 'small',
                        fullWidth: true
                    }
                }
            }
        },
        MuiDateTimePicker: {
            defaultProps: {
                slotProps: {
                    textField: {
                        size: 'small',
                        fullWidth: true
                    }
                }
            }
        },
        MuiDataGrid: {
            defaultProps: {
                density: 'compact',
                disableColumnMenu: true,
                showCellVerticalBorder: true,
                rowSelection: false,
                autoHeight: true,
                slotProps: {
                    loadingOverlay: {
                        variant: 'linear-progress',
                        noRowsVariant: 'linear-progress'
                    }
                }
            }
        },
        MuiTable: {
            styleOverrides: {
                root: {
                    borderCollapse: "collapse",
                    minWidth: 650,
                }
            }
        },
        MuiTableCell: {
            styleOverrides: {
                root: ({ theme }) => ({
                    padding: theme.spacing(0.5, 1),
                    fontSize: 12,
                    border: `1px solid ${theme.palette.divider}`,
                }),

                head: ({ theme }) => ({
                    fontWeight: 600,
                    backgroundColor: theme.palette.action.hover,
                }),
            }
        },
        MuiDialog: {
            styleOverrides: {
                root: ({ theme }) => ({
                    '& .MuiDialogTitle-root': {
                        padding: theme.spacing(1),
                        background: theme.palette.primary.main,
                        color: 'white'
                    },
                    '& .MuiDialogContent-root': {
                        padding: `${theme.spacing(1)}!important`,
                    },
                    '& .MuiDialogActions-root': {
                        padding: theme.spacing(1),
                        background: theme.palette.primary.main,
                        minHeight: 33,
                        '& .MuiButtonBase-root': {
                            color: 'white',
                            border: '2px solid rgba(255, 255, 255, 0.5)',
                        }
                    },
                })
            }
        }
        // MuiTreeItem: {
        //     styleOverrides: {
        //         label: {
        //             fontSize: '0.9rem'
        //         }
        //     }
        // }
    }
}, ruRU)