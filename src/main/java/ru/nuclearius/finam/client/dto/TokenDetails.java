package ru.nuclearius.finam.client.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class TokenDetails {
    private Instant createdAt;
    private Instant expiresAt;
    private List<MDPermission> mdPermissions;
    private List<String> accountIds;

    private boolean readonly;
    private boolean isActive;

    @Data
    public static class MDPermission {
        private QuoteLevel quoteLevel;
        private int delayMinutes;

        private String mic;
        private String country;
        private String continent;
        private boolean worldwide;

        public enum QuoteLevel {

            QUOTE_LEVEL_UNSPECIFIED,
            QUOTE_LEVEL_LAST_PRICE,
            QUOTE_LEVEL_BEST_BID_OFFER,
            QUOTE_LEVEL_DEPTH_OF_MARKET,
            QUOTE_LEVEL_DEPTH_OF_BOOK,
            QUOTE_LEVEL_ACCESS_FORBIDDEN,
            UNRECOGNIZED
        }
    }
}
