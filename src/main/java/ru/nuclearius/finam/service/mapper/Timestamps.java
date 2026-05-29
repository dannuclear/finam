package ru.nuclearius.finam.service.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Timestamp;

public class Timestamps {

    private static final Logger LOGGER = LoggerFactory.getLogger(Timestamps.class);
    static final long TIMESTAMP_SECONDS_MIN = -62135596800L;
    static final long TIMESTAMP_SECONDS_MAX = 253402300799L;
    static final int NANOS_PER_SECOND = 1000000000;

    public static final Timestamp MAX_VALUE = Timestamp.newBuilder().setSeconds(TIMESTAMP_SECONDS_MAX)
            .setNanos(NANOS_PER_SECOND - 1).build();
    public static final Timestamp MIN_VALUE = Timestamp.newBuilder().setSeconds(TIMESTAMP_SECONDS_MIN).setNanos(0)
            .build();

    /**
     * Sanitize Timestamps outside legal range where possible.
     */
    public static Timestamp sanitize(Timestamp t) {
        if (t.getNanos() < 0 || t.getNanos() >= NANOS_PER_SECOND) {
            throw new IllegalArgumentException(String.format(
                    "Timestamp is not valid. See proto definition for valid values. Seconds (%s) must be in range [-62,135,596,800, +253,402,300,799]. Nanos (%s) must be in range [0, +999,999,999].",
                    t.getSeconds(), t.getNanos()));
        }
        if (t.getSeconds() > TIMESTAMP_SECONDS_MAX) {
            LOGGER.warn(String.format(
                    "Cannot map to timestamp greater than allowed MAX: (%s). Using MAX_TIMESTAMP instead: (%s)", t,
                    Timestamps.MAX_VALUE));
            return MAX_VALUE;
        }
        if (t.getSeconds() < TIMESTAMP_SECONDS_MIN) {
            LOGGER.warn(String.format(
                    "Cannot map to timestamp less than allowed MIN: (%s). Using MIN_TIMESTAMP instead: (%s)", t,
                    Timestamps.MIN_VALUE));
            return MIN_VALUE;
        }

        return t;
    }

}