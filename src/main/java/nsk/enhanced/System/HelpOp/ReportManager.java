package nsk.enhanced.System.HelpOp;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportManager {

    private final EnhancedChat plugin = PluginInstance.getInstance();
    private final EnhancedLogger enhancedLogger = plugin.getEnhancedLogger();

    private final FileConfiguration config = plugin.getConfigFile();
    private final FileConfiguration translations = plugin.getTranslationsFile();

    private final Set<OfflinePlayer> operators = plugin.getServer().getOperators();

    private final List<Report> reports = new ArrayList<>();

    // --- --- --- --- --- --- --- //

    public ReportManager() {
        enhancedLogger.fine("Report Manager established.");
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public void addReport(Report report) {

        Player player = report.getReporter();

        try {
            reports.add(report);

            String message = translations.getString("EnhancedChat.messages.helpop_success", "<green>Report with id: <red><id></red> was sent.");
            player.sendMessage(MiniMessage.miniMessage().deserialize( message.replace("<id>", String.valueOf(reports.size())) ));

        } catch (Exception e) {
            enhancedLogger.severe("Report could not be added." + e.getMessage());

            String message = translations.getString("EnhancedChat.messages.helpop_failed", "<red>Report was not sent due to server error.");
            player.sendMessage(MiniMessage.miniMessage().deserialize( message.replace("<id>", String.valueOf(reports.size())) ));
        }
    }

    public void removeReport(Report report) {
        try {
            reports.remove(report);
            enhancedLogger.fine("Report was removed.");
        } catch (Exception e) {
            enhancedLogger.severe("Report could not be removed." + e.getMessage());
        }
    }

    public List<Report> getReports() {
        return reports;
    }

    public Report getReport(int id) {

        if (reports.get(id) != null) {
            return reports.get(id);
        } else {
            return null;
        }

    }
    public Report getReport(OfflinePlayer player) {

        for (Report report : reports) {
            if (report.getReporter() == player) {
                return report;
            }
        }

        return null;
    }

    // --- --- --- --- --- --- --- //

    public void messageAllOperators(Report report) {

        for (OfflinePlayer operator : operators) {
            if (operator.isOnline()) {
                Player player = operator.getPlayer();
                Player sender = report.getReporter();

                String message = translations.getString("EnhancedChat.messages.helpop_announce", "<yellow><green>%player_name%</green> just sent new Report with ID:</yellow> <red><id></red>");
                message = message.replace("<id>", String.valueOf(reports.size()));

                String placeholder = PlaceholderAPI.setPlaceholders(sender, message);
                Component l = MiniMessage.miniMessage().deserialize(placeholder);

                player.sendMessage(l);
            }
        }

    }

    public String viewReports() {

        StringBuilder list = new StringBuilder();

        list.append("\n\n<yellow>Report Manager: <green>all active reports</green></yellow").append("\n");
        int size = reports.size();
        for (int i=0; i < size; i++) {
            list.append("<gray>- ID:<green>").append(i).append("</green> ")
                    .append(reports.get(i).getReporter().getName())
                    .append(" reported <red>").append(reports.get(i).getSuspect().getName()).append("</red>.</gray>\n");
        }
        list.append("\n").append("<gray>For more information about each report, use command <green>/helpop show <id></green>.\n\n");

        return list.toString();

    }

}
