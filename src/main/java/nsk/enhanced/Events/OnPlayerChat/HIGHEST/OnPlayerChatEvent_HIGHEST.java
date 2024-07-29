package nsk.enhanced.Events.OnPlayerChat.HIGHEST;

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

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OnPlayerChatEvent_HIGHEST implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final boolean AntiPlayerReport = config.getBoolean("Chat.System.AntiPlayerReport.enabled", true);


    private final List<Character> characters = PluginInstance.getInstance().getCharacters();

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Character character = characters.stream().filter(c -> c.getUUID().equals(uuid)).findFirst().orElse(null);

        // --- --- --- --- Character --- --- --- --- //
        if (character == null) {
            character = new Character(uuid, false);
            PluginInstance.getInstance().addCharacter(character);
        }

        if (AntiPlayerReport && !event.isCancelled() ) {

            String format = event.getFormat();
            Set<Player> recipients = event.getRecipients();

            for (Player recipient : recipients) {
                recipient.sendMessage(MiniMessage.miniMessage().deserialize( format ));
            }
        }

    }

}