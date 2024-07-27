package nsk.enhanced.System.RunnableTask;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class AutoMessageTask implements Runnable{

    private final EnhancedChat enhancedChat;
    private int messageIndex = 0;

    public AutoMessageTask(EnhancedChat plugin) {
        this.enhancedChat = plugin;
    }

    @Override
    public void run() {
        FileConfiguration autoMessages = enhancedChat.getAutoMessagesFile();
        Set<String> keys = autoMessages.getConfigurationSection("AutoMessages.messages").getKeys(false);

        if (keys.isEmpty()) return;

        String[] messagesKey = keys.toArray(new String[0]);
        String currentKey = messagesKey[messageIndex];

        if (autoMessages.getBoolean("AutoMessages.messages." + currentKey + ".enabled")) {
            List<String> lines = autoMessages.getStringList("AutoMessages.messages." + currentKey + ".message");

                for (String line : lines) {

                    for (Player player : enhancedChat.getServer().getOnlinePlayers()) {

                        String text = PlaceholderAPI.setPlaceholders(player, line);
                        Component l = MiniMessage.miniMessage().deserialize(text);

                        player.sendMessage(l);
                    }
                }

                messageIndex = (messageIndex + 1) % messagesKey.length;
        }

    }
}
