package nsk.enhanced.Events.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.Player.Character;
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
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage("Player not found or is not online.");
                return true;
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
        } else if (command.getName().equalsIgnoreCase("ignore")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command."));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Usage: /ignore <player>");
                return true;
            } else {

                Player target = plugin.getServer().getPlayer(args[0]);

                if (target == null || !target.isOnline()) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found or is not online."));
                } else {
                    Player player = (Player) sender;
                    Character character = plugin.getCharacter(player.getUniqueId());

                    String answer = "<gold>" + target.getName() + "</gold> <green>is now ignored by you.";

                    if (character != null) {
                        if (character.isIgnored(target)) {
                            character.removeIgnoredPlayer(target);
                            answer = "<gold>" + target.getName() + "</gold> <green>is no longer ignored by you.";
                        } else {
                            character.addIgnoredPlayer(target);
                        }
                    }

                    player.sendMessage(MiniMessage.miniMessage().deserialize(answer));
                }

            }

        }

        return false;
    }

}
