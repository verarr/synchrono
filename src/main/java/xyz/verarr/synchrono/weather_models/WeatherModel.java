package xyz.verarr.synchrono.weather_models;

import net.minecraft.server.world.ServerWorld;
import xyz.verarr.synchrono.external_apis.OpenMeteoAPI;

public interface WeatherModel {
    void apply(OpenMeteoAPI.WeatherCode weatherCode, ServerWorld serverWorld);
}
