import type { TokenDetails } from '@shared/api/schema'
import dayjs from 'dayjs'

export type TokenInfoProps = {
    tokenDetails?: TokenDetails
}

export const TokenInfo = ({ tokenDetails }: TokenInfoProps) => {
    return (
        <div>
            <div>{dayjs(tokenDetails?.createdAt).format("DD.MM.YYYY HH:mm:ss")}</div>
            <div>{dayjs(tokenDetails?.expiresAt).format("DD.MM.YYYY HH:mm:ss")}</div>
        </div>
    )
}
