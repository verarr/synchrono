package xyz.verarr.synchrono.external_apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import xyz.verarr.synchrono.config.SynchronoConfig;

public class SunriseSunsetAPI {
    private static final String API_URL = "https://api.sunrisesunset.io/json";

    private record SunriseSunsetAPIQuery(LocalDate date, double latitude, double longitude){};
    public record  SunriseSunsetData(Instant sunrise, Instant sunset){};

    private static Map<SunriseSunsetAPIQuery, SunriseSunsetData> cache = new HashMap<>();

    public static @NotNull
    SunriseSunsetData query(LocalDate date, double latitude, double longitude) {
        return cache.computeIfAbsent(new SunriseSunsetAPIQuery(date, latitude, longitude),
                                     SunriseSunsetAPI::query);
    }

    private static @NotNull SunriseSunsetData query(SunriseSunsetAPIQuery details) {
        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri                 = new URI(API_URL
                                          + formatter.format("?date=%s&lat=%f&lng=%f&timezone=UTC&time_format=unix",
                                                             details.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                                             details.latitude, details.longitude));
        } catch (URISyntaxException e) { throw new RuntimeException(e); }

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
        Instant    sunrise, sunset;
        try {
            sunrise = Instant.ofEpochSecond(
                Long.parseLong(results.get(SynchronoConfig.sunriseProperty).getAsString()));
            sunset = Instant.ofEpochSecond(
                Long.parseLong(results.get(SynchronoConfig.sunsetProperty).getAsString()));
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException(e + " JSON: " + result);
        }

        return new SunriseSunsetData(sunrise, sunset);
    }
}
