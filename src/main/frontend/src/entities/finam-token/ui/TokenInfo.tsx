import type { TokenDetails } from '@shared/api/schema'
import { DefaultIcon } from '@shared/ui'
import dayjs from 'dayjs'

export type TokenInfoProps = {
    tokenDetails?: TokenDetails
}

export const TokenInfo = ({ tokenDetails }: TokenInfoProps) => {
    return (
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <DefaultIcon iconName='fa-circle' color={tokenDetails?.active ? "#71f13e" : "#ff0000"}></DefaultIcon>

            <div>
                <div>{dayjs(tokenDetails?.createdAt).format("DD.MM.YYYY HH:mm:ss")}</div>
                <div>{dayjs(tokenDetails?.expiresAt).format("DD.MM.YYYY HH:mm:ss")}</div>
            </div>
        </div>
    )
}
