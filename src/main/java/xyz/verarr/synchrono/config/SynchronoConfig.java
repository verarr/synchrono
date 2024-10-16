package xyz.verarr.synchrono.config;

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
                            .setJson5(true)
                            .build())
                    .build();

    @SerialEntry public static double latitude = 51.11d;
    @SerialEntry public static double longitude = 17.022222d;

    @SerialEntry public static boolean invert = false;
    @SerialEntry public static boolean gametime_enabled = true;

    @SerialEntry public static double scalar = 1.0f;
    @SerialEntry public static int offset_ticks = 0;

    @SerialEntry public static boolean brute_force = false;
    @SerialEntry public static String sunrise_property = "sunrise";
    @SerialEntry public static String sunset_property = "sunset";

    @SerialEntry public static boolean set_time = true;
    @SerialEntry public static boolean set_rate = true;
    @SerialEntry public static boolean prevent_sleep = true;

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
