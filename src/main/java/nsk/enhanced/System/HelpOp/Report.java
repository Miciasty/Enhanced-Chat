package nsk.enhanced.System.HelpOp;

import org.bukkit.entity.Player;

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

        long hours = TimeUnit.MILLISECONDS.toHours(this.timestamp);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(this.timestamp) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(this.timestamp) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // --- --- --- --- --- --- --- //


    @Override
    public String toString() {

        StringBuilder report = new StringBuilder();

        report.append("\n\n");
        report.append("<red>Report by: <gold>").append(getReporter().getName()).append("</gold></red>\n");
        report.append("<red>Created at: <aqua>").append(getFormattedTimestamp()).append("</aqua></red>\n");
        report.append("<green>Suspect: <gray>").append(getSuspect().getName()).append("</gray></green>\n");
        report.append("<green>Message: <gray>").append(getMessage()).append("</gray></green>\n\n");

        report.append("<gray>What would you like to do?</gray>");
        report.append("<gray>- <click:run_command:'/tp ").append(getReporter().getName()).append("'><click:run_command:'/gamemode spectator'><yellow>[Teleport to reporter]</yellow></click></click>\n");
        report.append("<gray>- <click:run_command:'/tp ").append(getSuspect().getName()).append("'><click:run_command:'/gamemode spectator'><red>[Teleport to suspect]</red></click></click>\n\n");

        return report.toString();

    }
}
