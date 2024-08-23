package utils;

import org.json.JSONObject;

import java.time.Instant;
import java.util.List;

public class ScheduleUtils {

    public static boolean isSortedByStartTime(List<JSONObject> programs) {
        return programs.stream()
                .map(program -> program.getLong("start_timestamp"))
                .reduce((prev, curr) -> {
                    if (prev == null) {
                        return curr;
                    } else if (curr >= prev) {
                        return curr;
                    } else {
                        return null;
                    }
                })
                .isPresent();
    }

    public static boolean hasCurrentProgram(List<JSONObject> programs) {
        long currentTime = Instant.now().getEpochSecond();

        return programs.stream()
                .anyMatch(program -> {
                    long startTime = program.getLong("start_timestamp");
                    long endTime = program.getLong("end_timestamp");
                    return startTime <= currentTime && currentTime <= endTime;
                });
    }

    public static boolean isWithin24Hours(List<JSONObject> programs) {
        long currentTime = Instant.now().getEpochSecond();
        long twentyFourHoursLater = currentTime + 24 * 3600;

        return programs.stream()
                .allMatch(program -> {
                    long startTime = program.getLong("start_timestamp");
                    long endTime = program.getLong("end_timestamp");

                    return startTime >= currentTime && endTime <= twentyFourHoursLater;
                });
    }
}
