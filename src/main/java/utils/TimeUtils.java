package utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtils {

    public static LocalDateTime convertToLocalDateTime(long timestamp, int utcOffset) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.ofTotalSeconds(utcOffset));
    }

    public static boolean verifyServerReturnCurrentTime(LocalDateTime serverTime, int utcOffset) {
        LocalDateTime now = LocalDateTime.now();
        return !serverTime.isBefore(now.minusSeconds(utcOffset)) && !serverTime.isAfter(now.plusSeconds(utcOffset));
    }

    public static boolean isIncorrectTimezone(LocalDateTime serverTime, int utcOffsetInSeconds) {
        LocalDateTime expectedTime = LocalDateTime.now(ZoneOffset.ofTotalSeconds(utcOffsetInSeconds));
        return !serverTime.isEqual(expectedTime);
    }

}
