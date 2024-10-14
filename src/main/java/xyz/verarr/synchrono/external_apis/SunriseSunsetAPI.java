package xyz.verarr.synchrono.external_apis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import xyz.verarr.synchrono.config.NewSynchronoConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(result.toString()).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e + " URL: " + uri.toString() + " JSON: " + result.toString());
        }

        if (!jsonObject.get("status").getAsString().equals("OK")) {
            throw new RuntimeException("Not OK Json from API: " + jsonObject.toString());
        }

        JsonObject results = jsonObject.get("results").getAsJsonObject();
        LocalTime sunrise, sunset;
        try {
            sunrise = LocalTime.parse(results.get(NewSynchronoConfig.sunrise_property).getAsString());
            sunset = LocalTime.parse(results.get(NewSynchronoConfig.sunset_property).getAsString());
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException(e + " JSON: " + result.toString());
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
