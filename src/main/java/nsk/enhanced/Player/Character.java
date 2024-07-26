package nsk.enhanced.Player;

import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Character {

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    // --- --- --- --- --- --- --- //

    private UUID uuid;
    private int chatMode;

    private boolean isBot;

    private final List<Warning> warnings = new ArrayList<>();
    private LinkedList<Long> messagesTimestamps = new LinkedList<>();
    private long muteUntil = 0;

    private int threatLevel;
    private final int MAX_WEIGHT = PluginInstance.getInstance().getConfigFile().getInt("Security.max_weight", 18);

    public Character(UUID uuid, int chatMode) {
        this.setUUID(uuid);
        this.setChatMode(chatMode);
        this.threatLevel = 0;

        this.isBot = true;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public void setChatMode(int chatMode) {
        this.chatMode = chatMode;
    }

    public void setBot(boolean bot) {
        this.isBot = bot;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public UUID getUUID() {
        return uuid;
    }
    public int getChatMode() {
        return chatMode;
    }

    public boolean isBot() {
        return isBot;
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

        if (warning.getLink() != null) {
            enhancedLogger.security(Bukkit.getPlayer(uuid) + " sent potentially dangerous link: " + warning.getLink());
        } else {
            enhancedLogger.character(Bukkit.getPlayer(uuid) + " received " + getAllWarningsOfCodeAsSize(code) + " " + code + " warning!");
        }

        calculateThreatLevel();
    }

    private void sendWarning(Warning warning) {
        Player player = Bukkit.getPlayer(uuid);
        String code = warning.getCode();

        if (player == null) {

            return;
        }

        String template, message;

        switch (code) {

            case "spam":
                int MuteDuration = config.getInt("Chat.Listener.AntiSpam.mute_duration", 20);

                template = translations.getString("EnhancedChat.alerts.mute", "<red>You have been muted for <time> seconds due to spamming.");
                message = template
                        .replace("<time>", String.valueOf(MuteDuration));

                player.sendMessage(message);
                break;

            case "flood":
                template = translations.getString("EnhancedChat.alerts.flood", "<red>Hey yo! Too many same characters in 1 word.");
                message = template;

                player.sendMessage(message);
                break;

            case "ad":
                template = translations.getString("EnhancedChat.alerts.advertisement", "<red>Hey! We don't allow advertisements here!");
                message = template;

                player.sendMessage(message);
                break;

            case "link":
                template = translations.getString("EnhancedChat.alerts.dangerous_link", "<red>Hmm.. did you just sent some weird suspicious link???");
                message = template;

                player.sendMessage(message);
                break;
        }
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
