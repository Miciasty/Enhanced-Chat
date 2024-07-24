package nsk.enhanced.System.Alerts;

import nsk.enhanced.System.PluginInstance;

public class Warning {

    private String message;
    private int level;

    public Warning(String message, int level) {
        setMessage(message);
        setLevel(level);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setMessage(String message) {
        this.message = message;
    }
    private void setLevel(int level) {
        try {
            if (level >= 0 && level <= 4) {
                this.level = level;
            } else {
                throw new IllegalArgumentException("Invalid level! Level should be between 0 and 4.");
            }
        } catch (Exception e) {
            PluginInstance.getInstance().getEnhancedLogger().severe(e.getMessage());
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public String getMessage() {
        return message;
    }
    public int getLevel() {
        return level;
    }
}
