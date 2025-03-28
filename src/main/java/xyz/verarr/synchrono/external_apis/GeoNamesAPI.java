package xyz.verarr.synchrono.external_apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.util.Formatter;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

public class GeoNamesAPI {
    private static final String API_URL = "http://api.geonames.org/timezoneJSON";

    public static @NotNull ZoneOffset query(double latitude, double longitude) {
        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri                 = new URI(
                API_URL
                + formatter.format("?lat=%f&lng=%f&username=synchrono_mod", latitude, longitude));
        } catch (URISyntaxException e) { throw new RuntimeException(e); }

        String result = HTTPHelper.get(uri);

        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(result).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e + " URL: " + uri + " JSON: " + result);
        }

        float rawOffset = jsonObject.get("rawOffset").getAsFloat();

        return ZoneOffset.of((rawOffset >= 0 ? "+" : "") + (int) rawOffset);
    }
}
