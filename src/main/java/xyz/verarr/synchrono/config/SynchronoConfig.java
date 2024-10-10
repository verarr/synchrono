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
    public static final String TIME = "a_time";
    public static final String SS_API = "b_ss_api";
    public static final String GAME_TIME = "c_game_time";
    public static final String DEBUG = "d_debug";

    @Comment(category = TIME, centered = true) public static Comment coords_title;
    @Comment(category = TIME) public static Comment coords_description;

    @Entry(category = TIME, min = -90d, max = 90d) public static double latitude = 51.11d;
    @Entry(category = TIME, min = -180d, max = 180d) public static double longitude = 17.022222d;
    @Entry(category = TIME) public static String timezone = "UTC";

    @Comment(category = TIME) public static Comment time_spacer1;

    @Comment(category = TIME, centered = true) public static Comment time_misc_title;

    @Comment(category = TIME) public static Comment invert_description;
    @Entry(category = TIME) public static boolean invert = false;

    @Comment(category = GAME_TIME) public static Comment gametime_enabled_description;
    @Entry(category = GAME_TIME) public static boolean gametime_enabled = true;

    @Comment(category = DEBUG) public static Comment debug_description;

    @Comment(category = DEBUG) public static Comment debug_spacer1;

    @Comment(category = DEBUG, centered = true) public static Comment time_modifiers_title;
    @Entry(category = DEBUG) public static float scalar = 1.0f;
    @Entry(category = DEBUG) public static int offset_seconds = 0;
    @Entry(category = DEBUG) public static boolean brute_force = false;

    @Comment(category = DEBUG) public static Comment debug_spacer2;

    @Comment(category = DEBUG, centered = true) public static Comment api_properties_title;
    @Comment(category = DEBUG) public static Comment api_properties_description;
    @Entry(category = DEBUG) public static String sunrise_property = "sunrise";
    @Entry(category = DEBUG) public static String sunset_property = "sunset";

    @Comment(category = DEBUG, centered = true) public static Comment debug_toggles_title;
    @Entry(category = DEBUG) public static boolean set_time = true;
    @Entry(category = DEBUG) public static boolean set_rate = true;
}