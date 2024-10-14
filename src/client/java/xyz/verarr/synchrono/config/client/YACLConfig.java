package xyz.verarr.synchrono.config.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.impl.controller.StringControllerBuilderImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import xyz.verarr.synchrono.config.NewSynchronoConfig;

public class YACLConfig extends NewSynchronoConfig {
    private static ConfigCategory time_category = ConfigCategory.createBuilder()
            .name(Text.literal("Time"))
            .tooltip(Text.literal("Settings for retrieving real-life time"))
            .group(OptionGroup.createBuilder()
                    .name(Text.literal("Server location"))
                    .description(OptionDescription.of(Text.literal("The location of the server with as much precision as desired")))
                    .option(Option.<Double>createBuilder()
                            .name(Text.literal("Latitude"))
                            .description(OptionDescription.of(Text.literal("The latitude of the server in decimal coordinates format")))
                            .binding(51.11d, () -> latitude, newVal -> latitude = newVal)
                            .controller(opt -> DoubleFieldControllerBuilder.create(opt)
                                    .range(-90d, 90d))
                            .build())
                    .option(Option.<Double>createBuilder()
                            .name(Text.literal("Longitude"))
                            .description(OptionDescription.of(Text.literal("The longitude of the server in decimal coordinates format")))
                            .binding(17.02d, () -> longitude, newVal -> longitude = newVal)
                            .controller(opt -> DoubleFieldControllerBuilder.create(opt)
                                    .range(-180d, 180d))
                            .build())
                    .option(Option.<String>createBuilder()
                            .name(Text.literal("Time zone"))
                            .description(OptionDescription.of(Text.literal("The time zone the server is located in. Make sure it is close enough to the location specified above. Examples are 'UTC' or 'Europe/Warsaw' This option will be removed in the future to be retrieved automatically.")))
                            .binding("UTC", () -> timezone, newVal -> timezone = newVal)
                            .controller(StringControllerBuilderImpl::new)
                            .build())
                    .build())
            .group(OptionGroup.createBuilder()
                    .name(Text.literal("Miscellaneous"))
                    .description(OptionDescription.of(Text.literal("Other options")))
                    .option(Option.<Boolean>createBuilder()
                            .name(Text.literal("Invert"))
                            .description(OptionDescription.of(Text.literal("Turn daytime into nighttime and vice-versa")))
                            .binding(false, () -> invert, newVal -> invert = newVal)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .build())
            .build();

    private static ConfigCategory gametime_category = ConfigCategory.createBuilder()
            .name(Text.literal("Game Time"))
            .tooltip(Text.literal("Settings for setting in-game time"))
            .option(Option.<Boolean>createBuilder()
                    .name(Text.literal("Enabled"))
                    .description(OptionDescription.of(Text.literal("Enable setting in-game time")))
                    .binding(true, () -> gametime_enabled, newVal -> gametime_enabled = newVal)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
            .group(OptionGroup.createBuilder()
                    .name(Text.literal("Time modifiers"))
                    .description(OptionDescription.of(Text.literal("These affect in-game tick values after all the calculations were made. You shouldn't really need them, but they are here in case you would like to use them.")))
                    .option(Option.<Double>createBuilder()
                            .name(Text.literal("Scalar"))
                            .description(OptionDescription.of(Text.literal("Scales the actual tick value. Not very useful.")))
                            .binding(1d, () -> scalar, newVal -> scalar = newVal)
                            .controller(DoubleFieldControllerBuilder::create)
                            .build())
                    .option(Option.<Integer>createBuilder()
                            .name(Text.literal("Offset (ticks)"))
                            .description(OptionDescription.of(Text.literal("Offsets the actual tick value. If your game is crashing because you started it before the first morning, set the offset to 24000 (this adjusts game time a whole day ahead).")))
                            .binding(0, () -> offset_ticks, newVal -> offset_ticks = newVal)
                            .controller(IntegerFieldControllerBuilder::create)
                            .build())
                    .build())
            .build();

    static YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Used for narration. Could be used to render a title in the future."))
            .category(time_category)
            .category(gametime_category)
            .save(HANDLER::save);

    static Screen getScreen(Screen parentScreen) {
        return builder.build().generateScreen(parentScreen);
    }
}
