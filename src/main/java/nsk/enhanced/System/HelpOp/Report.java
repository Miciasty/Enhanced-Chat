package nsk.enhanced.System.HelpOp;

import nsk.enhanced.System.PluginInstance;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Report {

    private final Player player;
    private final Player suspect;

    private final String message;
    private final long timestamp;

    // --- --- --- --- --- --- --- //

    public Report(Player player, Player suspect, String message) {
        this.player = player;
        this.suspect = suspect;
        this.message = message;
        this.timestamp = System.currentTimeMillis();

        PluginInstance.getInstance().getReportManager().messageAllOperators(this);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public Player getReporter() {
        return player;
    }

    public Player getSuspect() {
        return suspect;
    }

    // --- --- --- --- --- --- --- //

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {

        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        return formatter.format(date);
    }

    // --- --- --- --- --- --- --- //


    @Override
    public String toString() {

        StringBuilder report = new StringBuilder();

        report.append("\n\n");
        report.append("<green>Report by: <gold>").append(getReporter().getName()).append("</gold></green>\n");
        report.append("<green>Created at: <aqua>").append(getFormattedTimestamp()).append("</aqua></green>\n");
        report.append("<green>Suspect: <red>").append(getSuspect().getName()).append("</red></green>\n");
        report.append("<green>Message: <gray>").append(getMessage()).append("</gray></green>\n\n");

        report.append("<gray>What would you like to do?</gray>\n");
        report.append("<gray>- <click:run_command:'/tp ").append(getReporter().getName()).append("'><click:run_command:'/gamemode spectator'><yellow>[Teleport to reporter]</yellow></click></click>\n");
        report.append("<gray>- <click:run_command:'/tp ").append(getSuspect().getName()).append("'><click:run_command:'/gamemode spectator'><red>[Teleport to suspect]</red></click></click>\n\n");

        return report.toString();

    }
}
