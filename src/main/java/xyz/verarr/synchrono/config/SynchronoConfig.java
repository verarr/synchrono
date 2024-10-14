package xyz.verarr.synchrono.config;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Every option in a MidnightConfig class has to be public and static, so we can access it from other classes.
 * The config class also has to extend MidnightConfig*/

public class SynchronoConfig extends MidnightConfig {
    public static final String SS_API = "b_ss_api";
    public static final String GAME_TIME = "c_game_time";
    public static final String DEBUG = "d_debug";

    @Entry(category = GAME_TIME) public static boolean gametime_enabled = true;

    @Entry(category = GAME_TIME) public static float scalar = 1.0f;
    @Entry(category = GAME_TIME) public static int offset_ticks = 0;

    @Entry(category = DEBUG) public static boolean brute_force = false;

    @Entry(category = DEBUG) public static String sunrise_property = "sunrise";
    @Entry(category = DEBUG) public static String sunset_property = "sunset";

    @Entry(category = DEBUG) public static boolean set_time = true;
    @Entry(category = DEBUG) public static boolean set_rate = true;
}