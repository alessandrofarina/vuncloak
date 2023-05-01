package logger;

public class Logger {

    public enum Level {EXPLORING_PATH, FOUND_POM, FOUND_DEPENDENCY, FOUND_VULNERABILITY}

    private static boolean on = true;

    public static void log(Level level, String msg) {
        if(on)
            System.out.println(level.toString() + ": " + msg);
    }

    public static void turnOn() {
        on = true;
    }

    public static void turnOff() {
        on = false;
    }

}
