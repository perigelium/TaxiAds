package com.edisoninteractive.inrideads.Entities;

/**
 * Created by user on 04.01.2018.
 */

public class Macrocommands
{
    //block level commands
    public static final String SHOW_BLOCK = "show_block";
    public static final String HIDE_BLOCK = "hide_block";
    public static final String ON_PRESS = "on_press";
    public static final String ON_LONG_PRESS = "on_long_press";
    public static final String ON_ENTER_IDLE_MODE = "on_enter_idle_mode";
    public static final String ON_SHOW_INTERACTIVE = "on_show_interactive";
    public static final String SELECT_TOGGLE_BUTTON = "select_toggle_button";
    public static final String UNSELECT_TOGGLE_BUTTON = "unselect_toggle_button";
    public static final String SHOW_STD_CHANNEL = "show_std_channel";
    public static final String CHANNEL_NAV_BACK = "channel_nav_back";
    public static final String SET_BLOCK_PROPERTY = "set_block_property";
    public static final String SHOW_WEB_CONTENT = "show_web_content";
    public static final String SHOW_SHUTDOWN_AD = "show_shutdown_ad";
    public static final String SHOW_STARTUP_AD = "show_startup_ad";
    public static final String CLEAR_CHANNEL_CONTAINER = "clear_channel_container";

    //broadcast commands
    public static final String FREEZE_WEB_CONTENT = "freeze_web_content";
    public static final String UNFREEZE_WEB_CONTENT = "unfreeze_web_content";
    public static final String SET_GLOBAL_ARGUMENT = "set_global_argument";
    public static final String SHOW_RADMIN = "show_radmin";
    public static final String UPDATE_IDLE_TIMEOUT = "update_idle_timeout";
    public static final String KILL_DELAYED_COMMANDS = "kill_delayed_commands";
    public static final String KILL_POWER_DELAYED_COMMANDS = "kill_power_delayed_commands";
    public static final String SHUT_DOWN = "shut_down";
    public static final String SET_SCREEN_BRIGHTNESS = "set_screen_brightness";
    public static final String INCREASE_SCREEN_BRIGHTNESS = "increase_brightness";
    public static final String DECREASE_SCREEN_BRIGHTNESS = "decrease_brightness";
    public static final String TRACE = "trace";
    public static final String GOTO_IDLE_MODE = "goto_idle_mode";
    public static final String GOTO_SLEEP_MODE = "goto_sleep_mode";
    public static final String GOTO_STOPPED_MODE = "goto_stopped_mode";
    public static final String GOTO_WORK_MODE = "goto_work_mode";
    public static final String GOTO_PLAYBACK_MODE = "goto_playback_mode";
    public static final String START_EXTERNAL_PROCESS = "start_external_process";
    public static final String STOP_EXTERNAL_PROCESS = "stop_external_process";
    public static final String STOP_ALL_EXTERNAL_PROCESSES = "stop_all_external_processes";
    public static final String INCREASE_VOLUME = "increase_volume";
    public static final String DECREASE_VOLUME = "decrease_volume";
    public static final String SET_VOLUME = "set_volume";
    public static final String ENABLE_FACE_DETECTION = "enable_face_detection";
    public static final String DISABLE_FACE_DETECTION = "disable_face_detection";

    // broadcast new
    public static final String LOCATION_UPDATED = "location_updated";
    public static final String TAPPED = "tapped";
    public static final String RESTART_COMMAND = "restart_command";

    public static final String ON_START_IN_POWER_OFF_MODE = "on_start_in_power_off_mode";
    public static final String ON_POWER_CONNECTED = "on_power_on";
    public static final String ON_POWER_DISCONNECTED = "on_power_off";
}
