package xyz.verarr.synchrono.external_apis;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class OpenMeteoAPI {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    public static class WeatherCode {
        // weather code -> strength
        private static final Map<Integer, Float> RAIN_VALUES = ImmutableMap.of(
                51, 1.0f, 53, 1.33f, 55, 1.67f,
                61, 2.0f, 63, 2.33f, 65, 2.67f,
                80, 3.0f, 81, 3.33f, 82, 3.67f
        );
        private static final Map<Integer, Float> SLEET_VALUES = ImmutableMap.of(
                56, 1f, 57, 1.5f,
                66, 2f, 67, 2.5f
        );
        private static final Map<Integer, Float> SNOW_VALUES = ImmutableMap.of(
                71, 1f, 73, 1.33f, 75, 1.67f,
                77, 0.5f,
                85, 2f, 86, 2.5f
        );
        private static final Map<Integer, Float> THUNDERSTORM_VALUES = ImmutableMap.of(
                95, 1f,
                96, 2f, 99, 2.5f
        );
        private static final Map<Integer, Float> HAIL_VALUES = ImmutableMap.of(
                96, 1f,
                99, 2f
        );
        private static final Map<Integer, Float> FOG_VALUES = ImmutableMap.of(
                45, 1f,
                48, 2f
        );
        private static final Map<Integer, Float> CLOUDY_VALUES = ImmutableMap.of(
                1, 1f, 2, 2f, 3, 3f
        );

        private final int value;

        public WeatherCode(int value) {
            this.value = value;
        }

        private float getStrength(Map<Integer, Float> map) {
            return map.getOrDefault(value, 0f);
        }

        public float rainStrength() {return getStrength(RAIN_VALUES);}

        public float sleetStrength() {return getStrength(SLEET_VALUES);}

        public float snowStrength() {return getStrength(SNOW_VALUES);}

        public float thunderstormStrength() {return getStrength(THUNDERSTORM_VALUES);}

        public float hailStrength() {return getStrength(HAIL_VALUES);}

        public float fogStrength() {return getStrength(FOG_VALUES);}

        public float cloudStrength() {return getStrength(CLOUDY_VALUES);}

        public int getRawValue() {return value;}
    }

    /**
     * Query current real-time weather data from Open-Meteo
     * @param latitude  Latitude to query data for
     * @param longitude Longitude to query data for
     * @return Currently applicable WMO weather code
     * @throws RuntimeException if the API didn't return weather code in WMO
     * format (hopefully never)
     */
    public static @NotNull WeatherCode queryCurrent(double latitude, double longitude) throws RuntimeException {
        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri = new URI(API_URL + formatter.format("?latitude=%f&longitude=%f&current=weather_code",
                    latitude, longitude));
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

        JsonObject results = jsonObject.getAsJsonObject("current");
        JsonObject units = jsonObject.getAsJsonObject("current_units");

        if (!Objects.equals(units.get("weather_code").getAsString(), "wmo code")) {
            // we can only interpret WMO codes for now
            throw new RuntimeException("URL: " + uri + " JSON: " + result);
        }

        return new WeatherCode(results.get("weather_code").getAsInt());
    }
}
