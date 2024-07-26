package nsk.enhanced.Events.OnPlayerCommandPreprocess;

import net.kyori.adventure.text.Component;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class OnPlayerCommandPreprocessLOWEST implements Listener {

    private final EnhancedLogger enhancedLogger = PluginInstance.getInstance().getEnhancedLogger();

    private final FileConfiguration config = PluginInstance.getInstance().getConfigFile();
    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        List<String> ignoredCommands = config.getStringList("Commands.ignore");
        List<String> blockedCommands = config.getStringList("Commands.block");


        // --- --- --- --- Block commands --- --- --- --- //
        for (String blockedCommand : blockedCommands) {
            if (command.startsWith(blockedCommand.toLowerCase())) {

                Component message = Component.text(translations.getString("EnhancedChat.commands.blocked", "<gold>This command is blocked."));
                player.sendMessage(message);

                event.setCancelled(true);
            }
        }

        // --- --- --- --- Ignore Commands --- --- --- --- //
        for (String ignoredCommand : ignoredCommands) {
            if (command.startsWith(ignoredCommand.toLowerCase())) {
                event.setCancelled(true);
            }
        }

    }

}
