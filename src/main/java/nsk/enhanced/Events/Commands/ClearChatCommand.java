package nsk.enhanced.Events.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ClearChatCommand implements CommandExecutor {

    private final EnhancedChat plugin = PluginInstance.getInstance();

    private final FileConfiguration translations = plugin.getTranslationsFile();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("clear")) {

            if (sender.isOp() && args[0].equalsIgnoreCase("all")) {
                clearChat();
            } else if (sender instanceof Player) {
                Player player = (Player) sender;
                clearChat(player);

                String template = translations.getString("EnhancedChat.commands.player_clear", "<gold>Chat has been cleared.");
                player.sendMessage(MiniMessage.miniMessage().deserialize(template));
            }

            return true;
        }

        return false;
    }

    private void clearChat(Player player) {
        for (int i=0; i < 100; i++) {
            player.sendMessage(" ");
        }
    }

    private void clearChat() {

        EnhancedChat plugin = PluginInstance.getInstance();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearChat(player);

            String template = translations.getString("EnhancedChat.commands.all_clear", "<gold>Chat has been cleared for all players");
            player.sendMessage(MiniMessage.miniMessage().deserialize(template));
        }

    }
}
