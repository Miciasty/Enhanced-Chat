package nsk.enhanced.System.Alerts;

import nsk.enhanced.System.PluginInstance;

public class Warning {

    private String code;
    private int weight;

    public Warning(String code, int weight) {
        setCode(code);
        setWeight(weight);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setCode(String code) {
        this.code = code;
    }
    private void setWeight(int weight) {
        try {
            if (weight >= 0 ) {
                this.weight = weight;
            } else {
                this.weight = 0;
                throw new IllegalArgumentException("Invalid weight! Weight cannot be negative.");
            }
        } catch (Exception e) {
            PluginInstance.getInstance().getEnhancedLogger().severe(e.getMessage());
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public String getCode() {
        return code;
    }
    public int getWeight() {
        return weight;
    }
}
