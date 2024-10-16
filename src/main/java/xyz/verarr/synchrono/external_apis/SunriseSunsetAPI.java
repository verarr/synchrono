package xyz.verarr.synchrono.external_apis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import xyz.verarr.synchrono.config.SynchronoConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Formatter;
import java.util.Locale;

public class SunriseSunsetAPI {
    private static final String API_URL = "https://api.sunrisesunset.io/json";

    public static @NotNull SunriseSunsetData query(LocalDate date, double latitude, double longitude, ZoneId timezone) {
        SunriseSunsetData data = new SunriseSunsetData();

        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri = new URI(API_URL + formatter.format("?lat=%f&lng=%f&timezone=%s&time_format=24", latitude, longitude, timezone));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String result = HTTPHelper.get(uri);

        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(result).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e + " URL: " + uri + " JSON: " + result);
        }

        if (!jsonObject.get("status").getAsString().equals("OK")) {
            throw new RuntimeException("Not OK Json from API: " + jsonObject);
        }

        JsonObject results = jsonObject.get("results").getAsJsonObject();
        LocalTime sunrise, sunset;
        try {
            sunrise = LocalTime.parse(results.get(SynchronoConfig.sunrise_property).getAsString());
            sunset = LocalTime.parse(results.get(SynchronoConfig.sunset_property).getAsString());
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException(e + " JSON: " + result);
        }

        data.sunrise = sunrise;
        data.sunset = sunset;

        return data;
    }

    public static class SunriseSunsetData {
        public LocalTime sunrise;
        public LocalTime sunset;
    }
}
