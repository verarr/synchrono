package xyz.verarr.synchrono.config.client;

import dev.isxander.yacl3.api.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import xyz.verarr.synchrono.config.NewSynchronoConfig;

public class YACLConfig extends NewSynchronoConfig {
    static YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Used for narration. Could be used to render a title in the future."))
            .save(HANDLER::save);

    static Screen getScreen(Screen parentScreen) {
        return builder.build().generateScreen(parentScreen);
    }
}
