package ru.nuclearius.finam.utils;

import java.time.Duration;
import java.time.Instant;

import grpc.tradeapi.v1.marketdata.TimeFrame;

public class TimeFrameUtils {
    public static void validateTimeFrameDepth(
            TimeFrame timeFrame,
            Instant startTime,
            Instant endTime) {
        if (timeFrame == null || timeFrame == TimeFrame.TIME_FRAME_UNSPECIFIED) {
            throw new IllegalArgumentException("TimeFrame is not specified");
        }

        long requestedDays = Duration.between(startTime, endTime).toDays();

        long maxDays = getDepthDays(timeFrame);

        if (requestedDays > maxDays) {
            throw new IllegalArgumentException("Недопустимый период для таймфрейма " + timeFrame);
        }
    }

    public static long getDepthDays(TimeFrame timeFrame) {
        if (timeFrame == null || timeFrame == TimeFrame.TIME_FRAME_UNSPECIFIED) {
            throw new IllegalArgumentException("TimeFrame is not specified");
        }

        return switch (timeFrame) {
            case TIME_FRAME_M1 -> 7;

            case TIME_FRAME_M5,
                    TIME_FRAME_M15,
                    TIME_FRAME_M30,
                    TIME_FRAME_H1,
                    TIME_FRAME_H2,
                    TIME_FRAME_H4,
                    TIME_FRAME_H8 ->
                30;

            case TIME_FRAME_D -> 365;

            case TIME_FRAME_W,
                    TIME_FRAME_MN,
                    TIME_FRAME_QR ->
                365 * 5L;

            default -> throw new IllegalArgumentException(
                    "Unsupported timeframe: " + timeFrame);
        };
    }
}
