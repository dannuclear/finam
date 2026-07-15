import { TokenInfo, useTokenDetails } from "@entities/finam-token"

export const TokenInfoWidget = () => {
    const { data } = useTokenDetails()
    return (
        <TokenInfo tokenDetails={data} />
    )
}