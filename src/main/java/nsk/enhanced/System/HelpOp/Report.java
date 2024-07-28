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
}
