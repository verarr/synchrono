package xyz.verarr.synchrono.weather_models;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;

import xyz.verarr.synchrono.external_apis.OpenMeteoAPI;

public class VanillaWeatherModel implements WeatherModel {
    private static final VanillaWeatherModel instance = new VanillaWeatherModel();

    @Override
    public void apply(OpenMeteoAPI.WeatherCode weatherCode, ServerWorld serverWorld) {
        ServerWorldProperties worldProperties =
            (ServerWorldProperties) serverWorld.getLevelProperties();
        if (weatherCode.thunderstormStrength() > 0 || weatherCode.hailStrength() > 0) {
            worldProperties.setRaining(true);  // unsure if this is necessary
            worldProperties.setThundering(true);
        } else if (weatherCode.rainStrength() > 0 || weatherCode.snowStrength() > 0
                   || weatherCode.sleetStrength() > 0) {
            worldProperties.setRaining(true);
            worldProperties.setThundering(false);
        } else {
            worldProperties.setRaining(false);
            worldProperties.setThundering(false);
        }
    }

    public static VanillaWeatherModel getInstance() { return instance; }
}
