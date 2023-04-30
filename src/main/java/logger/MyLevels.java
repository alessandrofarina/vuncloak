package logger;

import java.util.logging.Level;

public class MyLevels extends Level {

    public static final Level PATH = new MyLevels("PATH", Level.INFO.intValue());
    public static final Level POM = new MyLevels("POM", Level.INFO.intValue());
    public static final Level DEPENDENCY = new MyLevels("DEPENDENCY", Level.INFO.intValue());
    public static final Level VULNERABILITY = new MyLevels("VULNERABILITY", Level.INFO.intValue());

    protected MyLevels(String name, int value) {
        super(name, value);
    }
}
