package xyz.verarr.synchrono.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;
import xyz.verarr.synchrono.external_apis.GeoTimeZoneAPI;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class SynchronoConfig {
    public static ConfigClassHandler<SynchronoConfig> HANDLER = ConfigClassHandler.createBuilder(SynchronoConfig.class)
            .id(Identifier.of("synchrono", "synchrono_config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir().resolve("synchrono.json5"))
                            .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
                            .appendGsonBuilder(gsonBuilder -> gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES))
                            .setJson5(true)
                            .build())
                    .build();

    @SerialEntry public static double latitude = 51.11d;
    @SerialEntry public static double longitude = 17.022222d;

    @SerialEntry public static boolean invert = false;
    @SerialEntry public static boolean gametimeEnabled = true;

    @SerialEntry public static double scalar = 1.0f;
    @SerialEntry public static int offsetTicks = 0;

    @SerialEntry public static boolean bruteForce = false;
    @SerialEntry public static String sunriseProperty = "sunrise";
    @SerialEntry public static String sunsetProperty = "sunset";

    @SerialEntry public static boolean setTime = true;
    @SerialEntry public static boolean setRate = true;
    @SerialEntry public static boolean preventSleep = true;
    @SerialEntry public static boolean removeCommands = true;

    private static class Coordinates {
        public double latitude;
        public double longitude;
        public Coordinates(double lat, double lng) {
            this.latitude = lat;
            this.longitude = lng;
        }
    }
    private static final Map<Coordinates, ZoneOffset> zoneOffsetCache = new HashMap<>(1);

    public static ZoneId timezone() {
        return ZoneId.ofOffset("UTC",
                zoneOffsetCache.computeIfAbsent(
                        new Coordinates(latitude, longitude),
                        coordinates -> GeoTimeZoneAPI.query(coordinates.latitude, coordinates.longitude)));
    }
}
