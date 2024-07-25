package nsk.enhanced.System.Alerts;

import java.util.logging.Level;

public class SecurityLevels extends Level {

    public static final Level CRITICAL = new SecurityLevels("CRITICAL", 9999);
    public static final Level SECURITY = new SecurityLevels("SECURITY", 990);
    public static final Level CHARACTER_WARNING = new SecurityLevels("CHARACTER_WARNING", 910);

    protected SecurityLevels(String name, int value) {
        super(name, value);
    }
}
