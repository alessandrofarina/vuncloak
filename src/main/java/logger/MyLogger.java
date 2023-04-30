package logger;

import java.util.logging.Logger;
import java.util.logging.Level;

public class MyLogger {
    
    private static final Logger LOGGER = Logger.getLogger("Logger");

    public static void log(Level level, String msg) {
        LOGGER.log(level, msg);
    }

    public static void turnOn() {
        LOGGER.setLevel(Level.ALL);
    }

    public static void turnOff() {
        LOGGER.setLevel(Level.OFF);
    }

}
