package nsk.enhanced.Events.OnPlayerChat.LOWEST;

import nsk.enhanced.Player.Character;
import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnPlayerChatEvent_LOWEST implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();
    private final FileConfiguration blacklist = PluginInstance.getInstance().getBlacklistFile();

    private final List<Character> characters = PluginInstance.getInstance().getCharacters();

    // --- --- --- --- --- //

    private final boolean AntiBot = config.getBoolean("Chat.Listener.AntiBot.enabled");
    private final String AntiAdvertising = config.getString("Chat.Listener.AntiAdvertising");

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Character character = characters.stream().filter(c -> c.getUUID().equals(uuid)).findFirst().orElse(null);

        // --- --- --- --- Character --- --- --- --- //
        if (character == null) {
            character = new Character(uuid,false);
            PluginInstance.getInstance().addCharacter( character );
        }

        // --- --- --- --- Is Muted? --- --- --- --- //
        if (character.isMuted()) {
            player.sendMessage("<gray>You are muted until: " + character.getMuteUntil());
            event.setCancelled(true);
        }

        // --- --- --- --- Anti Bot --- --- --- --- //
        if (AntiBot) {
            if (character.isBot()) {
                event.setCancelled(true);
            }
        }

        // --- --- --- --- Blacklisted --- --- --- --- //
        if (config.getBoolean("Chat.Listener.Blacklist.enabled")) {
            List<String> blacklist = config.getStringList("Words");
            String message = event.getMessage().toLowerCase();

            for (String word : blacklist) {
                if (message.contains(word.toLowerCase())) {

                    int weight = config.getInt("Chat.Listener.Blacklist.Warning.Weight", 1);
                    character.addWarning(new Warning("blacklist", weight));
                    event.setCancelled(true);
                }
            }
        }

        // --- --- --- --- Anti Advertising --- --- --- --- //
        if (config.getBoolean(AntiAdvertising + ".enabled")) {
            String message = event.getMessage();
            if (containsBlockedPhrase(message)) {

                int weight = config.getInt(AntiAdvertising + ".Warning.weight", 5);
                character.addWarning(new Warning("ad", weight));

                event.setCancelled(true);
            } else if (containsLink(message) && !isSafeLink(message)) {
                int weight = config.getInt(AntiAdvertising + ".Warning.suspiciousLink", 10);
                String link = extractLink(message);

                character.addWarning(new Warning("link", weight, link));

                event.setCancelled(true);
            }
        }

    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public boolean containsBlockedPhrase(String message) {
        List<String> blockedPhrases = config.getStringList(AntiAdvertising + ".blocked_phrases");
        for (String blockedPhrase : blockedPhrases) {
            if (message.toLowerCase().contains(blockedPhrase.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsLink(String message) {
        String urlPattern = "((http|https|ftp)://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/\\S*)?";
        return message.matches( ".*" + urlPattern + ".*") ;
    }

    public boolean isSafeLink(String message) {
        List<String> safeDomains = config.getStringList(AntiAdvertising + ".safe_domains");
        String urlPattern = "((http|https|ftp)://)?(www\\.)?([a-zA-Z0-9\\-\\.]+)\\.[a-zA-Z]{2,}(/\\S*)?";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String domain = matcher.group(3);
            if (safeDomains.contains(domain.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String extractLink(String message) {
        String urlPattern = "((http|https|ftp)://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/\\S*)?";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String domain = matcher.group();
        }
        return null;
    }

}
