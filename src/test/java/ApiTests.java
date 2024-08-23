import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ScheduleUtils;
import utils.TimeUtils;
import wrappers.HttpClientWrapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {
    private final HttpClientWrapper httpClientWrapper = new HttpClientWrapper();

    @Test
    @DisplayName("Verify that the server returns the current time")
    public void testServerReturnsCurrentTime() {
        Response response = httpClientWrapper.getTime();

        assertEquals(200, httpClientWrapper.getStatusCode(response), "The status code should be 200.");

        String responseBody = httpClientWrapper.getResponseBody(response);
        long timestamp = httpClientWrapper.extractTimestampFromResponse(responseBody);
        int utcOffset = new JSONObject(responseBody).getJSONObject("data").getInt("utc_offset");

        LocalDateTime serverTime = TimeUtils.convertToLocalDateTime(timestamp, utcOffset);

        assertTrue(TimeUtils.verifyServerReturnCurrentTime(serverTime, utcOffset), "The server time should be close to the current time.");
    }

    @Test
    @DisplayName("Verify the server handling incorrect timezone")
    public void testIncorrectTimezone() {
        Response response = httpClientWrapper.getTime();

        assertEquals(200, httpClientWrapper.getStatusCode(response), "The status code should be 200.");

        String responseBody = httpClientWrapper.getResponseBody(response);
        long timestamp = httpClientWrapper.extractTimestampFromResponse(responseBody);
        int utcOffset = new JSONObject(responseBody).getJSONObject("data").getInt("utc_offset");

        LocalDateTime serverTime = TimeUtils.convertToLocalDateTime(timestamp, utcOffset);

        boolean isIncorrectTimezone = TimeUtils.isIncorrectTimezone(serverTime, utcOffset);

        assertTrue(isIncorrectTimezone, "The server time should indicate an incorrect timezone handling.");
    }

    @Test
    @DisplayName("Verify the TV program schedule for specific video IDs")
    public void testVerifyPrograms() {
        String videoIds = "1639111,1585681,1639231";
        SoftAssertions softly = new SoftAssertions();
        Response response = httpClientWrapper.getTVSchedule(videoIds);

        assertEquals(200, httpClientWrapper.getStatusCode(response), "The status code should be 200.");

        String responseBody = response.getBody().asString();
        List<JSONObject> programs = httpClientWrapper.extractProgramsFromResponse(responseBody);

        // a) Verify programs are sorted by start_timestamp
        softly.assertThat(ScheduleUtils.isSortedByStartTime(programs))
                .as("Programs should be sorted by start_timestamp.")
                .isTrue();

        // b) Verify there is a program that matches the current time
        softly.assertThat(ScheduleUtils.hasCurrentProgram(programs))
                .as("There should be a program that matches the current time.")
                .isTrue();

        // c) Verify no programs are from the past or more than 24 hours into the future
        softly.assertThat(ScheduleUtils.isWithin24Hours(programs))
                .as("Programs should be within the next 24 hours.")
                .isTrue();

        softly.assertAll();
    }

    @Test
    @DisplayName("Verify the TV program schedule for invalid video IDs")
    public void testInvalidVideoIds() {
        String invalidVideoIds = "0000000,9999999";

        Response response = httpClientWrapper.getTVSchedule(invalidVideoIds);

        assertEquals(200, httpClientWrapper.getStatusCode(response), "The status code should be 200.");

        String responseBody = httpClientWrapper.getResponseBody(response);
        JSONObject jsonObject = new JSONObject(responseBody);

        assertTrue(
                jsonObject.getJSONArray("data").isEmpty() ||
                        jsonObject.getJSONArray("data").toList().stream()
                                .allMatch(dataObject -> new JSONObject(dataObject.toString()).getJSONArray("programs").isEmpty()),
                "The response should be empty or should not contain any valid programs for the invalid video IDs."
        );
    }

    @Test
    @DisplayName("Verify the TV program schedule for empty video ID")
    public void testEmptyVideoId() {
        String emptyVideoIds = "";
        Response response = httpClientWrapper.getTVSchedule(emptyVideoIds);

        assertEquals(200, httpClientWrapper.getStatusCode(response));

        String responseBody = httpClientWrapper.getResponseBody(response);
        JSONObject jsonObject = new JSONObject(responseBody);

        assertTrue(
                jsonObject.getJSONArray("data").isEmpty() ||
                        jsonObject.getJSONArray("data").toList().stream()
                                .allMatch(dataObject -> new JSONObject(dataObject.toString()).getJSONArray("programs").isEmpty()),
                "The response should be empty or should not contain any valid programs for the invalid video IDs."
        );
    }
}
