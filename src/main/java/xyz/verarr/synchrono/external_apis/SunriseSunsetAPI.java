package xyz.verarr.synchrono.external_apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
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

    private static class SunriseSunsetAPIQuery {
        LocalDate date;
        double    latitude, longitude;
        ZoneId    timezone;

        public SunriseSunsetAPIQuery(LocalDate date,
                                     double    latitude,
                                     double    longitude,
                                     ZoneId    timezone) {
            this.date      = date;
            this.latitude  = latitude;
            this.longitude = longitude;
            this.timezone  = timezone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SunriseSunsetAPIQuery that = (SunriseSunsetAPIQuery) o;
            return date.isEqual(that.date) && (Double.compare(that.latitude, latitude) == 0)
         && (Double.compare(that.longitude, longitude) == 0) && timezone.equals(that.timezone);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, latitude, longitude, timezone);
        }
    }

    private static Map<SunriseSunsetAPIQuery, SunriseSunsetData> cache = new HashMap<>();

    public static @NotNull
    SunriseSunsetData query(LocalDate date, double latitude, double longitude, ZoneId timezone) {
        return cache.computeIfAbsent(new SunriseSunsetAPIQuery(date, latitude, longitude, timezone),
                                     SunriseSunsetAPI::query);
    }

    private static @NotNull SunriseSunsetData query(SunriseSunsetAPIQuery details) {
        SunriseSunsetData data = new SunriseSunsetData();

        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri =
                new URI(API_URL
                        + formatter.format("?date=%s&lat=%f&lng=%f&timezone=%s&time_format=24",
                                           details.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                           details.latitude, details.longitude, details.timezone));
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
        LocalTime  sunrise, sunset;
        try {
            sunrise = LocalTime.parse(results.get(SynchronoConfig.sunriseProperty).getAsString());
            sunset  = LocalTime.parse(results.get(SynchronoConfig.sunsetProperty).getAsString());
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException(e + " JSON: " + result);
        }

        data.sunrise = sunrise;
        data.sunset  = sunset;

        return data;
    }

    public static class SunriseSunsetData {
        public LocalTime sunrise;
        public LocalTime sunset;
    }
}
