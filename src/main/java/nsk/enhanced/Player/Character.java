package nsk.enhanced.Player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Character {

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    // --- --- --- --- --- --- --- //

    private UUID uuid;
    private boolean isLocal;

    private boolean isBot;

    private final List<Warning> warnings = new ArrayList<>();
    private LinkedList<Long> messagesTimestamps = new LinkedList<>();
    private long muteUntil = 0;

    private int threatLevel;
    private final int MAX_WEIGHT = PluginInstance.getInstance().getConfigFile().getInt("Security.max_weight", 18);

    // --- --- --- --- --- --- --- //

    private final Set<Player> ignoredPlayers = new HashSet<>();

    public Character(UUID uuid, boolean isLocal) {
        this.setUUID(uuid);
        this.setLocal(isLocal);
        this.threatLevel = 0;

        this.isBot = true;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public void setLocal(boolean local) {
        this.isLocal = local;
    }

    public void setBot(boolean bot) {
        this.isBot = bot;
    }

    public boolean addIgnoredPlayer(Player player) {
        try {
            this.ignoredPlayers.add(player);
            return true;
        } catch (Exception e) {
            enhancedLogger.severe("Failed to add ignored player <red>" + player.getName() + "</red> for <gold>" + Bukkit.getPlayer(uuid).getName() + "</gold>: " + e.getMessage());
            return false;
        }
    }
    public boolean removeIgnoredPlayer(Player player) {
        try {
            this.ignoredPlayers.remove(player);
            return true;
        } catch (Exception e) {
            enhancedLogger.severe("Failed to remove ignored player <red>" + player.getName() + "</red> for <gold>" + Bukkit.getPlayer(uuid).getName() + "</gold>: " + e.getMessage());
            return false;
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public UUID getUUID() {
        return uuid;
    }
    public boolean isLocal() {
        return isLocal;
    }

    public boolean isBot() {
        return isBot;
    }

    public Set<Player> getIgnoredPlayers() {
        return ignoredPlayers;
    }
    public boolean isIgnored(Player player) {
        return ignoredPlayers.contains(player);
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

        sendWarning(warning);

        if (warning.getLink() != null) {
            enhancedLogger.security("<red>" + Bukkit.getPlayer(uuid).getName() + "</red> sent potentially dangerous link: " + warning.getLink());
        } else {
            enhancedLogger.character("<red>" + Bukkit.getPlayer(uuid).getName() + "</red> received " + getAllWarningsOfCodeAsSize(code) + " " + code + " warning!");
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

        String security = "\n<gradient:#582f74:#934fc2>(<#FF890E>\uD83D\uDD25</#FF890E>>-- --- --- --<<#479CE9>☠</#479CE9>) <bold>[Secu</bold></gradient><gradient:#934fc2:#582f74><bold>rity]</bold> (<#479CE9>☠</#479CE9>>-- --- --- --<<#FF890E>\uD83D\uDD25</#FF890E>)</gradient>\n\n     ";
        int lineSize = 48;

        switch (code) {

            case "blacklist":
                template = translations.getString("EnhancedChat.alerts.blacklist", "<yellow>You just said the forbidden word... We will remember that!");
                message = security + autoNewLiner(template, lineSize) + "\n";

                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                break;

            case "spam":
                int MuteDuration = config.getInt("Chat.Listener.AntiSpam.mute_duration", 20);

                template = translations.getString("EnhancedChat.alerts.mute", "<red>You have been muted for <time> seconds due to spamming.");
                template = autoNewLiner(template, lineSize) + "\n";
                message = security + template
                        .replace("<time>", String.valueOf(MuteDuration));

                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                break;

            case "flood":
                template = translations.getString("EnhancedChat.alerts.flood", "<red>Hey yo! Too many same characters in 1 word.");
                message = security + autoNewLiner(template, lineSize) + "\n";

                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                break;

            case "ad":
                template = translations.getString("EnhancedChat.alerts.advertisement", "<red>Hey! We don't allow advertisements here!");
                message = security + autoNewLiner(template, lineSize) + "\n";

                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
                break;

            case "link":
                template = translations.getString("EnhancedChat.alerts.dangerous_link", "<red>Hmm.. did you just sent some weird suspicious link???");
                message = security + autoNewLiner(template, lineSize) + "\n";

                player.sendMessage(MiniMessage.miniMessage().deserialize(message));
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

        if (threatLevel >= 4) {
            Player player = Bukkit.getPlayer(uuid);

            Bukkit.getScheduler().runTask(PluginInstance.getInstance(), () -> player.kickPlayer("You have been kicked due to high threat level."));
        }
    }

    private String autoNewLiner(String message, int lineSize) {
        StringBuilder result = new StringBuilder();
        double currentLineLength = 0;

        String[] words = message.split(" ");

        for (String word : words) {
            double wordLength = 0;

            for (char c : word.toCharArray()) {
                if (c == '.' || c == ',' || c == 'i' || c == 't' || c == 'f' || c == 'l' || c == ' ' || c == 'I') {
                    wordLength += 0.5;
                } else {
                    wordLength++;
                }
            }

            if (currentLineLength + wordLength > lineSize) {
                result.append("\n     ");
                currentLineLength = 0;
            }

            if (currentLineLength > 0) {
                result.append(" ");
                currentLineLength += 0.5;
            }

            result.append(word);
            currentLineLength += wordLength;
        }

        return result.toString();
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    // --- --- --- --- Anti Spam Listener --- --- --- --- //

    public String getFormattedMuteTime() {
        long currentTime = System.currentTimeMillis();
        long remainingTime = this.muteUntil - currentTime;

        long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

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

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("<green>Character of: <gold>").append(Bukkit.getPlayer(uuid).getName()).append("</gold>\n");
        sb.append("UUID: <aqua>").append(uuid.toString()).append("</aqua>\n");
        sb.append("Threat level: <red>").append(threatLevel).append("</red>\n");
        sb.append("IsMuted: <red>").append(isMuted()).append("</red>\n");
        sb.append("Warnings: \n");
        for (Warning warning : warnings) {
            sb.append("\t<gold>").append(warning.getCode()).append("</gold> weight: <gold>").append(warning.getWeight()).append("</gold>\n");
        }

        return sb.toString();
    }
}
