package nsk.enhanced.System;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nsk.enhanced.EnhancedChat;
import nsk.enhanced.System.Alerts.SecurityLevels;
import nsk.enhanced.System.PluginInstance;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.*;

public class EnhancedLogger extends Logger {

    private EnhancedChat plugin;

    public EnhancedLogger(EnhancedChat plugin) {
        super(plugin.getName(), null);
        this.plugin = plugin;
        setParent(Bukkit.getLogger());
        setLevel(Level.ALL);

        setUseParentHandlers(false);

        for (Handler handler : getHandlers()) {
            removeHandler(handler);
        }
        EnhancedHandler enhancedHandler = new EnhancedHandler();
        enhancedHandler.setFormatter(new SimpleFormatter());
        addHandler(enhancedHandler);
    }

    private class EnhancedHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;

            String prefix = "Enhanced Chat";

            String message = getFormatter().formatMessage(record);
            Level level = record.getLevel();

            Component casual = MiniMessage.miniMessage().deserialize("<gradient:#1f8eb2:#2dccff>["+ prefix +"]</gradient> " + message);

            if (level.equals(Level.SEVERE)) {
                Component severe = MiniMessage.miniMessage().deserialize("<gradient:#b24242:#ff5f5f>[" + prefix + "]</gradient> <#ffafaf>" + message);
                Bukkit.getConsoleSender().sendMessage(severe);

            } else if (level.equals(Level.WARNING)) {
                Component warning = MiniMessage.miniMessage().deserialize("<gradient:#b28724:#ffc234>[" + prefix + "]</gradient> <#ffe099>" + message);
                Bukkit.getConsoleSender().sendMessage(warning);

            } else if (level.equals(Level.FINE)) {
                Component fine = MiniMessage.miniMessage().deserialize("<gradient:#3ca800:#56f000>[" + prefix + "]</gradient> <#aaf77f>" + message);
                Bukkit.getConsoleSender().sendMessage(fine);

            } else if (level.equals(Level.CONFIG)) {
                Component dev = MiniMessage.miniMessage().deserialize("<gradient:#b28724:#ffc234>[NSK]</gradient><gradient:#1f8eb2:#2dccff> [Devmode] </gradient> <#ffe099>" + message);
                Bukkit.getConsoleSender().sendMessage(dev);

            } else if (level.equals(SecurityLevels.CHARACTER_WARNING)) {
                Component character = MiniMessage.miniMessage().deserialize("<gradient:#b28724:#ffc234>[Character]</gradient> <#ffe099>" + message);
                Bukkit.getConsoleSender().sendMessage(character);

            } else if (level.equals(SecurityLevels.SECURITY)) {
                Component security = MiniMessage.miniMessage().deserialize("<#ff5f5f>⚠</#ff5f5f> <gradient:#6f54b4:#896ece><bold>[SECURITY BREACHED] <#ff5f5f>⚠</#ff5f5f></bold></gradient> <#bdaee4>" + message);
                Bukkit.getConsoleSender().sendMessage(security);

            } else if (level.equals(SecurityLevels.HTTP)) {
                Component character = MiniMessage.miniMessage().deserialize("<gradient:#1f8eb2:#2dccff>[Link Validation]</gradient> <#ffe099>" + message);
                Bukkit.getConsoleSender().sendMessage(character);
            } else {
                Bukkit.getConsoleSender().sendMessage(casual);
            }

        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

    }

    public void character(String message) {
        log(SecurityLevels.CHARACTER_WARNING, message);
    }

    public void security(String message) {
        log(SecurityLevels.SECURITY, message);
    }

    public void http(String message) {
        log(SecurityLevels.HTTP, message);
    }

}
