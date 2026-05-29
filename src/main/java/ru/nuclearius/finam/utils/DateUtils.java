package ru.nuclearius.finam.utils;

import java.time.Duration;
import java.time.Instant;

import com.google.protobuf.Timestamp;
import com.google.type.Interval;

import grpc.tradeapi.v1.marketdata.TimeFrame;

public final class DateUtils {

    private DateUtils() {
    }

    public static Timestamp toTimestamp(Instant instant) {
        if (instant == null)
            return null;

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null)
            return null;

        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos());
    }

    public static Interval toInterval(Instant start, Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End must be after start");
        }

        Interval.Builder builder = Interval.newBuilder();

        if (start != null) {
            builder.setStartTime(toTimestamp(start));
        }
        if (end != null) {
            builder.setEndTime(toTimestamp(end));
        }

        return builder.build();
    }

    public static Duration toDuration(TimeFrame timeframe) {
        if (timeframe == null) {
            throw new IllegalArgumentException("TimeFrame is null");
        }

        switch (timeframe) {
            case TIME_FRAME_M1:
                return Duration.ofMinutes(1);
            case TIME_FRAME_M5:
                return Duration.ofMinutes(5);
            case TIME_FRAME_M15:
                return Duration.ofMinutes(15);
            case TIME_FRAME_M30:
                return Duration.ofMinutes(30);

            case TIME_FRAME_H1:
                return Duration.ofHours(1);
            case TIME_FRAME_H2:
                return Duration.ofHours(2);
            case TIME_FRAME_H4:
                return Duration.ofHours(4);
            case TIME_FRAME_H8:
                return Duration.ofHours(8);

            case TIME_FRAME_D:
                return Duration.ofDays(1);
            case TIME_FRAME_W:
                return Duration.ofDays(7);

            case TIME_FRAME_MN:
                return Duration.ofDays(30); // условно
            case TIME_FRAME_QR:
                return Duration.ofDays(90); // условно

            case TIME_FRAME_UNSPECIFIED:
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
    }
}
