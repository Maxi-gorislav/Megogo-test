package wrappers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import loader.YamlConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HttpClientWrapper {

    private static final String BASE_URI;

    static {
        BASE_URI = YamlConfigLoader.loadBaseUri();
        RestAssured.baseURI = BASE_URI;
    }

    public Response getTime() {
        return RestAssured
                .given()
                .when()
                .get("/time")
                .then()
                .extract()
                .response();
    }

    public Response getTVSchedule(String videoIds) {
        return RestAssured
                .given()
                .queryParam("video_ids", videoIds)
                .when()
                .get("/channel")
                .then()
                .extract()
                .response();
    }

    public int getStatusCode(Response response) {
        return response.getStatusCode();
    }

    public String getResponseBody(Response response) {
        return response.getBody().asString();
    }

    public long extractTimestampFromResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONObject("data").getLong("timestamp_gmt");
    }

    public List<JSONObject> extractProgramsFromResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        return IntStream.range(0, dataArray.length())
                .mapToObj(dataArray::getJSONObject)
                .filter(dataObject -> dataObject.has("programs"))
                .flatMap(dataObject -> {
                    JSONArray programsArray = dataObject.getJSONArray("programs");
                    return IntStream.range(0, programsArray.length())
                            .mapToObj(programsArray::getJSONObject);
                })
                .collect(Collectors.toList());
    }
}
