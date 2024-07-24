package nsk.enhanced.Events;

import nsk.enhanced.Player.Character;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OnPlayerChatEvent implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    private final boolean WorldChat = config.getBoolean("Chat.type.World");
    private final boolean LocalChat = config.getBoolean("Chat.type.Local");
    private final boolean PrivateChat = config.getBoolean("Chat.type.Private");

    private final List<Character> characters = PluginInstance.getInstance().getCharacters();

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if (WorldChat) {
            Player player = event.getPlayer();
            Set<Player> recipients = event.getRecipients();
            recipients.removeIf(p -> !p.getWorld().equals(player.getWorld()));

            String message = event.getMessage();
            enhancedLogger.info("<green>[World]</green> " + player.getName() + "<reset>: " + message);

        }

        if (config.getBoolean(LocalChat + ".enable")) {
            Player player = event.getPlayer();
            Set<Player> recipients = event.getRecipients();
            double range = config.getDouble(LocalChat + ".range");

            event.getRecipients().removeIf(p -> !p.getWorld().equals(player.getWorld()) || p.getLocation().distance(player.getLocation()) > range);

            String message = event.getMessage();
            enhancedLogger.info("<aqua>[Local]</aqua> " + player.getName() + "<reset>: " + message);

        }

    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
    }

}
