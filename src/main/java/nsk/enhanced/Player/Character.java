package nsk.enhanced.Player;

import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Character {

    private UUID uuid;
    private int chatMode;

    private final List<Warning> warnings = new ArrayList<>();
    private LinkedList<Long> messagesTimestamps = new LinkedList<>();
    private long muteUntil = 0;

    private int threatLevel;
    private final int MAX_WEIGHT = PluginInstance.getInstance().getConfigFile().getInt("Security.max_weight", 18);

    public Character(UUID uuid, int chatMode) {
        this.setUUID(uuid);
        this.setChatMode(chatMode);
        this.threatLevel = 0;
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

    public int getAllWarningsOfCodeAsSize(String code) {
        int count = 0;
        for (Warning warning : warnings) {
            if (warning.getCode().equals(code)) {
                count++;
            }
        }
        return count;
    }

    public List<Warning> getAllWarningsOfCode(String code) {
        List<Warning> warnings2 = new ArrayList<>();
        for (Warning warning : warnings) {
            if (warning.getCode().equals(code)) {
                warnings2.add(warning);
            }
        }
        return warnings2;
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
        String code = warning.getCode();

        PluginInstance.getInstance().getEnhancedLogger().character(Bukkit.getPlayer(uuid) + " received " + getAllWarningsOfCodeAsSize(code) + " " + code + " warning!");

        calculateThreatLevel();
    }

    // --- --- --- --- --- --- --- //

    public int getThreatLevel() {
        return threatLevel;
    }

    private void calculateThreatLevel() {
        int totalWeight = 0;
        for (Warning warning : warnings) {
            totalWeight += warning.getWeight();
        }

        double percentage = (double) totalWeight / MAX_WEIGHT;

        this.threatLevel = (int) Math.round(percentage * 4);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    // --- --- --- --- Anti Spam Listener --- --- --- --- //

    public LinkedList<Long> getMessagesTimestamps() {
        return messagesTimestamps;
    }

    public long getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(long muteUntil) {
        this.muteUntil = muteUntil;
    }

    public boolean isMuted() {
        return System.currentTimeMillis() < muteUntil;
    }
}
