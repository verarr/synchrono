package xyz.verarr.synchrono.config;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import xyz.verarr.synchrono.external_apis.GeoNamesAPI;

public class SynchronoConfig {
    public static ConfigClassHandler<SynchronoConfig> HANDLER =
        ConfigClassHandler.createBuilder(SynchronoConfig.class)
            .id(Identifier.of("synchrono", "synchrono_config"))
            .serializer(
                config
                -> GsonConfigSerializerBuilder.create(config)
                       .setPath(
                           FabricLoader.getInstance().getConfigDir().resolve("synchrono.json5"))
                       .appendGsonBuilder(
                           GsonBuilder::setPrettyPrinting)  // not needed, pretty print by default
                       .appendGsonBuilder(gsonBuilder
                                          -> gsonBuilder.setFieldNamingPolicy(
                                              FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES))
                       .setJson5(true)
                       .build())
            .build();

    @SerialEntry public static double latitude  = 51.11d;
    @SerialEntry public static double longitude = 17.022222d;

    @SerialEntry public static boolean invert          = false;
    @SerialEntry public static boolean gametimeEnabled = true;

    @SerialEntry public static boolean adjustedPhantomSpawnsIntegration = true;

    @SerialEntry public static double scalar      = 1.0f;
    @SerialEntry public static int    offsetTicks = 0;

    @SerialEntry public static boolean weatherEnabled = true;

    public enum WeatherModel implements NameableEnum {
        VANILLA;
        // TODO: more

        @Override
        public Text getDisplayName() {
            return switch (this) {
                case VANILLA -> Text.literal("Vanilla");
            };
        }
    }

    @SerialEntry public static WeatherModel weatherModel = WeatherModel.VANILLA;

    @SerialEntry public static boolean bruteForce      = false;
    @SerialEntry public static String  sunriseProperty = "sunrise";
    @SerialEntry public static String  sunsetProperty  = "sunset";

    @SerialEntry public static boolean setTime        = true;
    @SerialEntry public static boolean setRate        = true;
    @SerialEntry public static boolean preventSleep   = true;
    @SerialEntry public static boolean removeCommands = true;

    private static class Coordinates {
        public double latitude;
        public double longitude;

        public Coordinates(double lat, double lng) {
            this.latitude  = lat;
            this.longitude = lng;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinates that = (Coordinates) o;
            return Double.compare(that.latitude, latitude) == 0
         && Double.compare(that.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }
    }

    private static final Map<Coordinates, ZoneOffset> zoneOffsetCache = new HashMap<>(1);

    public static ZoneId timezone() {
        return ZoneId.ofOffset(
            "UTC",
            zoneOffsetCache.computeIfAbsent(
                new Coordinates(latitude, longitude),
                coordinates -> GeoNamesAPI.query(coordinates.latitude, coordinates.longitude)));
    }
}
