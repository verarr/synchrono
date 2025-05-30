package xyz.verarr.synchrono.config.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.fabricmc.loader.api.FabricLoader;
import xyz.verarr.synchrono.config.SynchronoConfig;

public class YACLConfig extends SynchronoConfig {
    private static final ConfigCategory time_category =
        ConfigCategory.createBuilder()
            .name(Text.translatable("synchrono.config.time.title"))
            .tooltip(Text.translatable("synchrono.config.time.tooltip"))
            .group(
                OptionGroup.createBuilder()
                    .name(Text.translatable("synchrono.config.time.location.title"))
                    .description(OptionDescription.of(
                        Text.translatable("synchrono.config.time.location.description")))
                    .option(
                        Option.<Double>createBuilder()
                            .name(Text.translatable("synchrono.config.time.location.latitude.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.time.location.latitude.description")))
                            .binding(51.11d, () -> latitude, newVal -> latitude = newVal)
                            .controller(
                                opt -> DoubleFieldControllerBuilder.create(opt).range(-90d, 90d))
                            .build())
                    .option(
                        Option.<Double>createBuilder()
                            .name(
                                Text.translatable("synchrono.config.time.location.longitude.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.time.location.longitude.description")))
                            .binding(17.02d, () -> longitude, newVal -> longitude = newVal)
                            .controller(
                                opt -> DoubleFieldControllerBuilder.create(opt).range(-180d, 180d))
                            .build())
                    .build())
            .group(OptionGroup.createBuilder()
                       .name(Text.translatable("synchrono.config.time.miscellaneous.title"))
                       .description(OptionDescription.of(
                           Text.translatable("synchrono.config.time.miscellaneous.description")))
                       .option(Option.<Boolean>createBuilder()
                                   .name(Text.translatable(
                                       "synchrono.config.time.miscellaneous.invert.name"))
                                   .description(OptionDescription.of(Text.translatable(
                                       "synchrono.config.time.miscellaneous.invert.description")))
                                   .binding(false, () -> invert, newVal -> invert = newVal)
                                   .controller(TickBoxControllerBuilder::create)
                                   .build())
                       .build())
            .build();

    private static final ConfigCategory gametime_category =
        ConfigCategory.createBuilder()
            .name(Text.translatable("synchrono.config.gametime.title"))
            .tooltip(Text.translatable("synchrono.config.gametime.tooltip"))
            .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("synchrono.config.gametime.enabled.name"))
                        .description(OptionDescription.of(
                            Text.translatable("synchrono.config.gametime.enabled.description")))
                        .binding(true, () -> gametimeEnabled, newVal -> gametimeEnabled = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
            .group(OptionGroup.createBuilder()
                       .name(Text.translatable("synchrono.config.gametime.modifiers.title"))
                       .description(OptionDescription.of(
                           Text.translatable("synchrono.config.gametime.modifiers.description")))
                       .option(Option.<Double>createBuilder()
                                   .name(Text.translatable(
                                       "synchrono.config.gametime.modifiers.scalar.name"))
                                   .description(OptionDescription.of(Text.translatable(
                                       "synchrono.config.gametime.modifiers.scalar.description")))
                                   .binding(1d, () -> scalar, newVal -> scalar = newVal)
                                   .controller(DoubleFieldControllerBuilder::create)
                                   .build())
                       .option(
                           Option.<Integer>createBuilder()
                               .name(Text.translatable(
                                   "synchrono.config.gametime.modifiers.offset_ticks.name"))
                               .description(OptionDescription.of(Text.translatable(
                                   "synchrono.config.gametime.modifiers.offset_ticks.description")))
                               .binding(0, () -> offsetTicks, newVal -> offsetTicks = newVal)
                               .controller(IntegerFieldControllerBuilder::create)
                               .build())
                       .build())
            .group(
                OptionGroup.createBuilder()
                    .name(Text.translatable("synchrono.config.gametime.miscellaneous.title"))
                    .description(OptionDescription.of(
                        Text.translatable("synchrono.config.gametime.miscellaneous.description")))
                    .option(
                        Option.<Boolean>createBuilder()
                            .name(Text.translatable(
                                "synchrono.config.gametime.miscellaneous.adjustedphantomspawns.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.gametime.miscellaneous.adjustedphantomspawns.description")))
                            .binding(true,
                                     ()
                                         -> adjustedPhantomSpawnsIntegration,
                                     newVal -> adjustedPhantomSpawnsIntegration = newVal)
                            .controller(TickBoxControllerBuilder::create)
                            .available(
                                FabricLoader.getInstance().isModLoaded("adjusted_phantom_spawns"))
                            .build())
                    .build())
            .build();

    private static final ConfigCategory weather_category =
        ConfigCategory.createBuilder()
            .name(Text.translatable("synchrono.config.weather.title"))
            .tooltip(Text.translatable("synchrono.config.weather.tooltip"))
            .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("synchrono.config.weather.enabled.name"))
                        .description(OptionDescription.of(
                            Text.translatable("synchrono.config.weather.enabled.description")))
                        .binding(true, () -> weatherEnabled, newVal -> weatherEnabled = newVal)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
            .option(Option.<WeatherModel>createBuilder()
                        .name(Text.translatable("synchrono.config.weather.model.name"))
                        .description(OptionDescription.of(
                            Text.translatable("synchrono.config.weather.model.description")))
                        .binding(WeatherModel.VANILLA,
                                 ()
                                     -> weatherModel,
                                 newVal -> weatherModel = newVal)
                        .controller(
                            opt -> EnumControllerBuilder.create(opt).enumClass(WeatherModel.class))
                        .build())
            .build();

    private static final ConfigCategory debug_category =
        ConfigCategory.createBuilder()
            .name(Text.translatable("synchrono.config.debug.title"))
            .tooltip(Text.translatable("synchrono.config.debug.tooltip"))
            .group(
                OptionGroup.createBuilder()
                    .name(Text.translatable("synchrono.config.debug.toggles.title"))
                    .description(OptionDescription.of(
                        Text.translatable("synchrono.config.debug.toggles.description")))
                    .option(
                        Option.<Boolean>createBuilder()
                            .name(Text.translatable("synchrono.config.debug.toggles.set_time.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.debug.toggles.set_time.description")))
                            .binding(true, () -> setTime, newVal -> setTime = newVal)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable(
                                    "synchrono.config.debug.toggles.brute_force.name"))
                                .description(OptionDescription.of(Text.translatable(
                                    "synchrono.config.debug.toggles.brute_force.description")))
                                .binding(false, () -> bruteForce, newVal -> bruteForce = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                    .option(
                        Option.<Boolean>createBuilder()
                            .name(Text.translatable("synchrono.config.debug.toggles.set_rate.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.debug.toggles.set_rate.description")))
                            .binding(true, () -> setRate, newVal -> setRate = newVal)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable(
                                    "synchrono.config.debug.toggles.prevent_sleep.name"))
                                .description(OptionDescription.of(Text.translatable(
                                    "synchrono.config.debug.toggles.prevent_sleep.description")))
                                .binding(true, () -> preventSleep, newVal -> preventSleep = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                    .option(
                        Option.<Boolean>createBuilder()
                            .name(Text.translatable(
                                "synchrono.config.debug.toggles.remove_commands.name"))
                            .description(OptionDescription.of(Text.translatable(
                                "synchrono.config.debug.toggles.remove_commands.description")))
                            .binding(true, () -> removeCommands, newVal -> removeCommands = newVal)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .build())
            .group(
                OptionGroup.createBuilder()
                    .name(Text.translatable("synchrono.config.debug.api_properties.title"))
                    .description(OptionDescription.of(
                        Text.translatable("synchrono.config.debug.api_properties.description")))
                    .option(Option.<String>createBuilder()
                                .name(Text.translatable(
                                    "synchrono.config.debug.api_properties.sunrise.name"))
                                .description(OptionDescription.of(Text.translatable(
                                    "synchrono.config.debug.api_properties.sunrise.description")))
                                .binding("sunrise",
                                         ()
                                             -> sunriseProperty,
                                         newVal -> sunriseProperty = newVal)
                                .controller(StringControllerBuilder::create)
                                .build())
                    .option(Option.<String>createBuilder()
                                .name(Text.translatable(
                                    "synchrono.config.debug.api_properties.sunset.name"))
                                .description(OptionDescription.of(Text.translatable(
                                    "synchrono.config.debug.api_properties.sunset.description")))
                                .binding("sunset",
                                         ()
                                             -> sunsetProperty,
                                         newVal -> sunsetProperty = newVal)
                                .controller(StringControllerBuilder::create)
                                .build())
                    .build())
            .build();

    static YetAnotherConfigLib.Builder builder =
        YetAnotherConfigLib.createBuilder()
            .title(Text.translatable("synchrono.config.title"))
            .category(time_category)
            .category(gametime_category)
            .category(weather_category)
            .category(debug_category)
            .save(HANDLER::save);

    static Screen getScreen(Screen parentScreen) {
        return builder.build().generateScreen(parentScreen);
    }
}
