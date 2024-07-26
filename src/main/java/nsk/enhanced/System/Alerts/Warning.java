package nsk.enhanced.System.Alerts;

import nsk.enhanced.System.PluginInstance;

public class Warning {

    private String code;
    private int weight;

    private String link;                    // Optional only for suspicious links

    public Warning(String code, int weight) {
        setCode(code);
        setWeight(weight);
    }

    public Warning(String code, int weight, String link) {
        setCode(code);
        setWeight(weight);
        setLink(link);
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
    private void setLink(String link) {
        this.link = link;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public String getCode() {
        return code;
    }
    public int getWeight() {
        return weight;
    }
    public String getLink() {
        return link;
    }
}
