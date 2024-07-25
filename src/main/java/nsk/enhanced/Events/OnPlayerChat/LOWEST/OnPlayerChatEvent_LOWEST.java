package nsk.enhanced.Events.OnPlayerChat.LOWEST;

import nsk.enhanced.Player.Character;
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

public class OnPlayerChatEvent_LOWEST implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final List<Character> characters = PluginInstance.getInstance().getCharacters();

    // --- --- --- --- --- //

    private final boolean AntiBot = config.getBoolean("Chat.Listener.AntiBot.enabled");

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Character character = characters.stream().filter(c -> c.getUUID().equals(uuid)).findFirst().orElse(null);

        // --- --- --- --- Character --- --- --- --- //
        if (character == null) {
            character = new Character(uuid,0);
            PluginInstance.getInstance().addCharacter( character );
        }

        // --- --- --- --- Anti Bot --- --- --- --- //
        if (AntiBot) {
            if (character.isBot()) {
                event.setCancelled(true);
            }
        }

    }
}
