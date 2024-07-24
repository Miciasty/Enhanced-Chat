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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PrivateMessageCommand implements CommandExecutor {

    private final EnhancedChat plugin = PluginInstance.getInstance();

    private final FileConfiguration translations = plugin.getTranslationsFile();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /msg <player> <message>");
                return false;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("Player not found or is not online.");
                return false;
            }


            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String template = translations.getString("EnhancedChat.messages.private", "(☁) [<sender> -> <target>]: ");
            String directMessage = template
                    .replace("<sender>", sender.getName())
                    .replace("<target>", target.getName())
                    + message;

            Component msg = MiniMessage.miniMessage().deserialize(directMessage);

            sender.sendMessage(msg);
            target.sendMessage(msg);

            return true;
        }

        return false;
    }

}
