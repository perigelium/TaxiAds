package com.edisoninteractive.inrideads.Entities;

public class Macro {

    public class Event {

        // Dispatched through Event Manager only in order to notify app components. Never used in config file.
        public class Internal {
            public static final String POWER_DISCONNECTED = "power_disconnected_event";
            public static final String POWER_CONNECTED = "power_connected_event";

        }

        //
        public class Config {
            public static final String ON_POWER_ON = "on_power_on";
            public static final String ON_POWER_OFF = "on_power_off";
        }

    }

    public class Command{

        public class Internal {
            public static final String GOTO_PLAYBACK_MODE = "goto_playback_mode";
            public static final String GOTO_STOPPED_MODE = "goto_stopped_mode";
        }


        public class Config {
            public static final String KILL_DELAYED_COMMANDS = "kill_delayed_commands";
            public static final String KILL_POWER_DELAYED_COMMANDS = "kill_power_delayed_commands";
            public static final String SHUT_DOWN = "shut_down";
            public static final String REBOOT = "reboot";
            public static final String SET_SCREEN_BRIGHTNESS = "set_screen_brightness";
            public static final String INCREASE_SCREEN_BRIGHTNESS = "increase_brightness";
            public static final String DECREASE_SCREEN_BRIGHTNESS = "decrease_brightness";
            public static final String TRACE = "trace";
            public static final String INCREASE_VOLUME = "increase_volume";
            public static final String DECREASE_VOLUME = "decrease_volume";
            public static final String SET_VOLUME = "set_volume";
            public static final String UPDATE_IDLE_TIMEOUT = "update_idle_timeout";
        }

    }





}
