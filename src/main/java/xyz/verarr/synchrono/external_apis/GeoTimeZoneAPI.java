package xyz.verarr.synchrono.external_apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.Formatter;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

public class GeoTimeZoneAPI {
    private static final String API_URL = "https://api.geotimezone.com/public/timezone";

    public static @NotNull ZoneId query(double latitude, double longitude) {
        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri                 = new URI(API_URL
                                          + formatter.format("?latitude=%f&longitude=%f", latitude, longitude));
        } catch (URISyntaxException e) { throw new RuntimeException(e); }

        String result = HTTPHelper.get(uri);

        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(result).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e + " URL: " + uri + " JSON: " + result);
        }

        return ZoneId.of(jsonObject.get("iana_timezone").getAsString());
    }
}
