package nsk.enhanced.Events.OnPlayerChat.LOW;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OnPlayerChatEvent_LOW implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final boolean WorldChat     = config.getBoolean("Chat.Type.World");
    private final String LocalChat      = "Chat.Type.Local";
    private final String PrivateChat    = "Chat.Type.Private";

    private final String AntiSpam       = "Chat.Listener.AntiSpam";
    private final String AntiFlood      = "Chat.Listener.AntiFlood";
    private boolean AntiCap             = config.getBoolean("Chat.Listener.AntiCap.enabled");

    private final List<Character> characters = PluginInstance.getInstance().getCharacters();

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

        // --- --- --- --- Anti Spam --- --- --- --- //
        if (config.getBoolean(AntiSpam + ".enabled")) {
            if (isSpam(character, player)) {

                int weight = config.getInt(AntiSpam + ".Warning.weight", 2);
                character.addWarning(new Warning("spam", weight));

                event.setCancelled(true);
                return;
            }
        }

        // --- --- --- --- Anti Cap --- --- --- --- //
        if (AntiCap) {
            String message = event.getMessage();
            if (!message.isEmpty()) {
                message = java.lang.Character.toUpperCase(message.charAt(0)) + message.substring(1);
                event.setMessage(message);
            }
        }

        // --- --- --- --- Anti Flood --- --- --- --- //
        if (config.getBoolean(AntiFlood + ".enabled")) {
            String message = event.getMessage();
            if (hasTooManyRepeatingCharacters(message, config.getInt(AntiFlood + ".max_characters"))) {

                int weight = config.getInt(AntiFlood + ".Warning.weight", 1);
                character.addWarning(new Warning("flood", weight));

                event.setCancelled(true);
                return;
            }

        }

        // --- --- --- --- World Chat --- --- --- --- //
        if (WorldChat) {
            Set<Player> recipients = event.getRecipients();
            recipients.removeIf(p -> !p.getWorld().equals(player.getWorld()));

            String message = event.getMessage();
            Component formattedMessage = MiniMessage.miniMessage().deserialize("<green>[World]</green> " + player.getName() + "<reset>: " + message);
            String messageString = LegacyComponentSerializer.legacySection().serialize(formattedMessage);

            event.setFormat(messageString);
        }

        // --- --- --- --- Local Chat --- --- --- --- //
        if (config.getBoolean(LocalChat + ".enabled") && character.isLocal()) {
            enhancedLogger.info("Local chat active.");
            Set<Player> recipients = event.getRecipients();
            double range = config.getDouble(LocalChat + ".range");

            recipients.removeIf(p -> !p.getWorld().equals(player.getWorld()) || p.getLocation().distance(player.getLocation()) > range);

            String message = event.getMessage();
            Component formattedMessage = MiniMessage.miniMessage().deserialize("<aqua>[Local]</aqua> " + player.getName() + "<reset>: " + message);

            String messageString = LegacyComponentSerializer.legacySection().serialize(formattedMessage);

            event.setFormat(messageString);

        }

        character.getMessagesTimestamps().add(System.currentTimeMillis());

    }


    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private boolean isSpam(Character character, Player player) {
        long currentTime = System.currentTimeMillis();
        long muteUntil = character.getMuteUntil();

        if (currentTime < muteUntil) {
            return true;
        }

        LinkedList<Long> messageTimestamps = character.getMessagesTimestamps();

        int TimeFrame = config.getInt(AntiSpam + ".time_frame") * 1000;             // Time frame
        int MaxMessages = config.getInt(AntiSpam + ".max_messages");                // Max messages
        int MuteDuration = config.getInt(AntiSpam + ".mute_duration") * 1000;       // Mute duration

        messageTimestamps.removeIf(timestamp -> currentTime - timestamp > TimeFrame);

        if (messageTimestamps.size() >= MaxMessages) {

            character.setMuteUntil(currentTime + MuteDuration);

            return true;
        }

        return false;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private boolean hasTooManyRepeatingCharacters(String message, int maxCharacters) {
        int count = 1;
        for (int i = 1; i < message.length(); i++) {
            if (message.charAt(i) == message.charAt(i - 1)) {
                count++;
                if (count > maxCharacters) {
                    return true;
                }
            } else {
                count = 1;
            }
        }

        return false;
    }
}
