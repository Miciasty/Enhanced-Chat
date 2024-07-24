package nsk.enhanced.Player;

import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.PluginInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Character {

    private UUID uuid;
    private int chatMode;

    private final List<Warning> warnings = new ArrayList<>();

    private int threatLevel = 0;
    private final int MAX_WEIGHT = PluginInstance.getInstance().getConfigFile().getInt("Security.max_weight", 18);

    public Character(UUID uuid, int chatMode) {
        this.setUUID(uuid);
        this.setChatMode(chatMode);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public void setChatMode(int chatMode) {
        this.chatMode = chatMode;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public UUID getUUID() {
        return uuid;
    }
    public int getChatMode() {
        return chatMode;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public List<Warning> getWarnings() {
        return warnings;
    }

    public Warning getWarning(int index) {
        if (index >= 0 && index < warnings.size()) {
            return warnings.get(index);
        } else {
            return null;
        }
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
        calculateThreatLevel();
    }

    // --- --- --- --- --- --- --- //

    public int getThreatLevel() {
        return threatLevel;
    }

    private void calculateThreatLevel() {
        int totalWeight = 0;
        for (Warning warning : warnings) {
            totalWeight += warning.getLevel();
        }

        double percentage = (double) totalWeight / MAX_WEIGHT;

        this.threatLevel = (int) Math.round(percentage * 4);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //
}
