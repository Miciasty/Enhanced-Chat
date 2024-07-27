package nsk.enhanced.Events.Commands;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class AnnouncementsCommand implements CommandExecutor {

    private final EnhancedChat plugin = PluginInstance.getInstance();

    private final FileConfiguration translations = plugin.getTranslationsFile();
    private final FileConfiguration announcements = plugin.getAnnouncementsFile();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("announcement")) {

            List<String> lines = announcements.getStringList("Type.announcement" );

            for (String line : lines) {

                for (Player player : plugin.getServer().getOnlinePlayers()) {

                    String text = PlaceholderAPI.setPlaceholders(player, line);
                    Component l = MiniMessage.miniMessage().deserialize(text);

                    player.sendMessage(l);
                }
            }

        } else if (command.getName().equalsIgnoreCase("warning")) {

            List<String> lines = announcements.getStringList("Type.warning" );

            for (String line : lines) {

                for (Player player : plugin.getServer().getOnlinePlayers()) {

                    String text = PlaceholderAPI.setPlaceholders(player, line);
                    Component l = MiniMessage.miniMessage().deserialize(text);

                    player.sendMessage(l);
                }
            }

        } else if (command.getName().equalsIgnoreCase("broadcast")) {

            List<String> lines = announcements.getStringList("Type.broadcast" );

            for (String line : lines) {

                for (Player player : plugin.getServer().getOnlinePlayers()) {

                    String text = PlaceholderAPI.setPlaceholders(player, line);
                    Component l = MiniMessage.miniMessage().deserialize(text);

                    player.sendMessage(l);
                }
            }

        }

        return false;
    }


}
