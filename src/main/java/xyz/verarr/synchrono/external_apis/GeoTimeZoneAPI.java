package xyz.verarr.synchrono.external_apis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.util.Formatter;
import java.util.Locale;

public class GeoTimeZoneAPI {
    private static final String API_URL = "https://api.geotimezone.com/public/timezone";

    public static @NotNull ZoneOffset query(double latitude, double longitude) {
        URI uri;
        try {
            Formatter formatter = new Formatter(Locale.ROOT);
            uri = new URI(API_URL + formatter.format("?latitude=%f&longitude=%f", latitude, longitude));
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

        return ZoneOffset.of(jsonObject.get("offset").getAsString().replaceFirst("UTC", "").replaceFirst(":.*", ""));
    }
}
