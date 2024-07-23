package nsk.enhanced.Events;

import nsk.enhanced.Player.Character;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class OnPlayerChatEvent implements Listener {

    private final FileConfiguration translations = PluginInstance.getInstance().getTranslationsFile();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
    }

}
