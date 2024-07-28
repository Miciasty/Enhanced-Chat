package nsk.enhanced.Events.OnPlayerChat.LOWEST;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.Player.Character;
import nsk.enhanced.System.Alerts.Warning;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.net.HttpURLConnection;
import java.net.URL;
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
    private final String AntiAdvertising = "Chat.Listener.AntiAdvertising";

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
            Component muted = MiniMessage.miniMessage().deserialize(translations.getString("EnhancedChat.messages.muted", "<gray>You are muted until: <red>") + character.getFormattedMuteTime());
            player.sendMessage(muted);
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
            List<String> blacklist = this.blacklist.getStringList("Words");
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

                character.addWarning(new Warning("link", 1, link));

                event.setCancelled(true);
            }
        }

        //

        if (!character.isMuted() && config.getBoolean("Chat.System.Mention.enabled", true)) {
            String message = event.getMessage();
            String mentionPattern = "@[^\\s]+";
            Pattern pattern = Pattern.compile(mentionPattern);

            Matcher matcher = pattern.matcher(message);

            StringBuilder newMessage = new StringBuilder();
            int lastEnd = 0;

            while (matcher.find()) {
                String mention = matcher.group().substring(1);

                newMessage.append(message, lastEnd, matcher.start());

                if (mention.equalsIgnoreCase("everyone")) {

                    newMessage.append("<aqua>@").append(mention).append("</aqua>");

                    for (Player target : PluginInstance.getInstance().getServer().getOnlinePlayers()) {

                        target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

                    }

                } else {

                    boolean playerFound = false;

                    for ( Player target : PluginInstance.getInstance().getServer().getOnlinePlayers()) {
                        if (target.getName().equalsIgnoreCase(mention)) {

                            newMessage.append("<yellow>@").append(target.getName()).append("</yellow>");

                            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

                            playerFound = true;
                            break;
                        }
                    }

                    if (!playerFound) {
                        newMessage.append(matcher.group());
                    }
                }



                lastEnd = matcher.end();
            }

            newMessage.append(message.substring(lastEnd));
            event.setMessage(newMessage.toString());
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
        String urlPattern = "((http|https|ftp)://)?(www\\.)?([a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,4})(/\\S*)?";
        Pattern pattern = Pattern.compile(urlPattern);

        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String url      = matcher.group();
            String protocol = matcher.group(1);
            String domain   = matcher.group(4);

            if (domain != null) {

                if (protocol != null && protocol.equalsIgnoreCase("ftp://")) return false;

                if (isValidLink(url, protocol)) {

                    for (String safeDomain : safeDomains) {
                        if (domain.equalsIgnoreCase(safeDomain)) {
                            return true;
                        }
                    }

                } else {
                    return true;
                }

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

    private boolean isValidLink(String oldURL, String protocol) {
        try {

            if (protocol == null) {
                oldURL = "http://" + oldURL;
            }

            URL url = new URL(oldURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();

            enhancedLogger.http("Response code: <red>" + responseCode + "</red>");

            return (200 <= responseCode && responseCode <= 399);

        } catch (Exception e) {
            enhancedLogger.severe(e.getMessage());
            return false;
        }
    }

}
