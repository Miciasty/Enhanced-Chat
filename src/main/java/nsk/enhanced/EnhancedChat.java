package nsk.enhanced;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import nsk.enhanced.Events.OnPlayerChatEvent;
import nsk.enhanced.Player.Character;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.PluginInstance;
import nsk.enhanced.Tags.Annotations;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class EnhancedChat extends JavaPlugin implements Listener {

    private EnhancedLogger enhancedLogger;

    private File configFile;
    private FileConfiguration config;

    private File translationsFile;
    private FileConfiguration translations;

    private SessionFactory sessionFactory;

    private boolean devmode = false;

    // --- --- --- --- --- --- --- --- --- --- //

    private final List<Character> characters = new ArrayList<>();

    // --- --- --- --- --- --- --- --- --- --- //

    @Override
    public void onEnable() {

        PluginInstance.setInstance(this);

        enhancedLogger = new EnhancedLogger(this);

        loadConfiguration();
        loadTranslations();

        // configureHibernate();

        // --- --- --- --- // Events Managers & Listeners // --- --- --- --- //
        OnPlayerChatEvent onPlayerChatEvent = new OnPlayerChatEvent();
        try {
            getServer().getPluginManager().registerEvents(onPlayerChatEvent, this);
            enhancedLogger.fine("onPlayerChatEvent registered");
            enhancedLogger.fine("onPlayerCommandPreprocessEvent registered.");
        } catch (Exception e) {
            enhancedLogger.severe("Registration onPlayerChatEvent failed! - " + e.getMessage());
            enhancedLogger.severe("Registration onPlayerCommandPreprocessEvent failed! - " + e.getMessage());
        }

        PluginCommand command = this.getCommand("ec");
        if (command != null) {
            command.setExecutor(this);
            enhancedLogger.fine("Commands registered.");
        } else {
            enhancedLogger.severe("Command 'eo' is not registered.");
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void loadConfiguration() {
        enhancedLogger.warning("Loading configuration...");
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

    }
    private FileConfiguration getConfigFile() {
        return config;
    }
    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            enhancedLogger.log(Level.SEVERE, "Failed to save config file", e);
        }
    }

    private void loadTranslations() {
        enhancedLogger.warning("Loading translations...");
        translationsFile = new File(getDataFolder(), "translations.yml");
        if (!translationsFile.exists()) {
            translationsFile.getParentFile().mkdirs();
            saveResource("translations.yml", false);
        }

        translations = YamlConfiguration.loadConfiguration(translationsFile);

    }
    public FileConfiguration getTranslationsFile() {
        return translations;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void reloadConfiguration() {
        try {

            loadConfiguration();
            loadTranslations();

            enhancedLogger.fine("Reloaded configuration");

        } catch (Exception e) {
            enhancedLogger.severe("Failed to reload configuration. - " + e);
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void configureHibernate() {
        enhancedLogger.warning("Configuring Hibernate...");
        try {
            String dialect  = config.getString("EnhancedChat.database.dialect");

            String address  = config.getString("EnhancedChat.database.address");
            String port     = config.getString("EnhancedChat.database.port");
            String database = config.getString("EnhancedChat.database.database");

            String username = config.getString("EnhancedChat.database.username");
            String password = config.getString("EnhancedChat.database.password");

            String show_sql     = config.getString("EnhancedChat.hibernate.show_sql");
            String format_sql   = config.getString("EnhancedChat.hibernate.format_sql");
            String sql_comments = config.getString("EnhancedChat.hibernate.sql_comments");

            Configuration cfg = new Configuration()
                    .setProperty("hibernate.dialect", "org.hibernate.dialect." + dialect)
                    .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")

                    .setProperty("hibernate.connection.url", "jdbc:mysql://" + address + ":" + port + "/" + database)
                    .setProperty("hibernate.connection.username", username)
                    .setProperty("hibernate.connection.password", password)

                    .setProperty("hibernate.hbm2ddl.auto", "update")
                    .setProperty("hibernate.show_sql", show_sql)
                    .setProperty("hibernate.format_sql", format_sql)
                    .setProperty("hibernate.use_sql_comments", sql_comments);

            // cfg.addAnnotatedClass(Region.class);

            if (cfg.buildSessionFactory() != null) {
                sessionFactory = cfg.buildSessionFactory();
            } else {
                throw new IllegalStateException("Could not create session factory");
            }

        } catch (Exception e) {
            enhancedLogger.severe("Could not create session factory - " + e.getMessage());
        }
        enhancedLogger.fine("Hibernate loaded");
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    public List<Character> getCharacters() {
        return characters;
    }

    public Character getCharacter(UUID uuid) {
        for (Character character : characters) {
            if (character.getUUID().equals(uuid)) {
                return character;
            }
        }
        return null;
    }

    public boolean isCharacter(Character character) {
        return characters.contains(character);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        addCharacter( new Character(uuid,0));
    }

    public void addCharacter(Character character) {
        try {
            characters.add(character);
        } catch (Exception e) {
            enhancedLogger.severe("Could not add character <aqua>" + character.getUUID() + "</aqua> - " + e.getMessage());
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Character character = getCharacter(uuid);
        if (character != null) {
            removeCharacter(character);
        }
    }

    public void removeCharacter(Character character) {
        try {
            characters.remove(character);
        } catch (Exception e) {
            enhancedLogger.severe("Could not remove character <aqua>" + character.getUUID() + "</aqua> - " + e.getMessage());
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //
    /*
                            888888     88b 88     888888     88     888888     Yb  dP
                            88__       88Yb88       88       88       88        YbdP
                            88""       88 Y88       88       88       88         8P
                            888888     88  Y8       88       88       88         dP
    */
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private <T> void saveEntity(T entity) {
        enhancedLogger.warning("Preparing to save entity: " + entity.getClass().getSimpleName());
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            enhancedLogger.info("Saving entity: " + entity.getClass().getSimpleName());
            session.saveOrUpdate(entity);
            session.getTransaction().commit();
            enhancedLogger.fine("Saved entity: " + entity.getClass().getSimpleName());
        } catch (Exception e) {
            enhancedLogger.severe("Saving entity failed - " + e.getMessage());
        }
    }
    public <T> CompletableFuture<Void> saveEntityAsync(T entity) {

        return CompletableFuture.runAsync(() -> {
            enhancedLogger.warning("Saving entity: " + entity);
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                enhancedLogger.info("Saving entity: " + entity.getClass().getSimpleName());
                session.saveOrUpdate(entity);
                session.getTransaction().commit();
                enhancedLogger.fine("Saved entity: " + entity.getClass().getSimpleName());
                session.close();
            } catch (Exception e) {
                enhancedLogger.severe("Saving entity failed - " + e.getMessage());
            }

        });
    }
    public <T> CompletableFuture<Void> saveAllEntitiesFromListAsync(List<T> entities) {

        return CompletableFuture.runAsync(() -> {
            enhancedLogger.warning("Saving entities from the list: " + entities);
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                for (T entity : entities) {
                    enhancedLogger.info("Saving entity: " + entity.getClass().getSimpleName());
                    session.saveOrUpdate(entity);
                }

                session.getTransaction().commit();
                enhancedLogger.fine("Saved entities from the list: " + entities);
                session.close();
            } catch (Exception e) {
                enhancedLogger.severe("Saving entities failed - " + e.getMessage());
            }
        });
    }
    public <T> CompletableFuture<Boolean> saveAllEntitiesWithRetry(List<T> entities, int maxAttempts) {

        return saveAllEntitiesFromListAsync(entities).handle((result, ex) -> {
            if (ex == null) {
                return CompletableFuture.completedFuture(true);
            } else if (maxAttempts > 1) {
                enhancedLogger.warning("Save failed, retrying... Attempts left: " + maxAttempts);
                return saveAllEntitiesWithRetry(entities, maxAttempts - 1);
            } else {
                enhancedLogger.severe("Save failed after maximum attempts: " + ex);
                return CompletableFuture.completedFuture(false);
            }
        }).thenCompose(result -> result);

    }

    private <T> void deleteEntity(T entity) {
        enhancedLogger.warning("Preparing to delete entity: " + entity.getClass().getSimpleName());
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            enhancedLogger.info("Deleting entity: " + entity.getClass().getSimpleName());
            session.delete(entity);
            session.getTransaction().commit();
            enhancedLogger.fine("Deleted entity: " + entity.getClass().getSimpleName());
        } catch (Exception e) {
            enhancedLogger.severe("Deleting entity failed - " + e.getMessage());
        }
    }
    public <T> CompletableFuture<Void> deleteEntityAsync(T entity) {

        return CompletableFuture.runAsync(() -> {
            enhancedLogger.warning("Preparing to delete entity: " + entity.getClass().getSimpleName());
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                enhancedLogger.info("Deleting entity: " + entity.getClass().getSimpleName());
                session.delete(entity);
                session.getTransaction().commit();
                enhancedLogger.fine("Deleted entity: " + entity.getClass().getSimpleName());
                session.close();
            } catch (Exception e) {
                enhancedLogger.severe("Deleting entity failed - " + e.getMessage());
            }
        });
    }
    public <T> CompletableFuture<Void> deleteAllEntitiesFromListAsync(List<T> entities) {

        return CompletableFuture.runAsync(() -> {
            enhancedLogger.warning("Preparing to delete entities from the list: " + entities);
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();

                for (T entity : entities) {
                    enhancedLogger.info("Deleting entity: " + entity.getClass().getSimpleName());
                    session.delete(entity);
                }

                session.getTransaction().commit();
                enhancedLogger.fine("Deleted entities from the list: " + entities);
                session.close();
            } catch (Exception e) {
                enhancedLogger.severe("Deleting entities failed - " + e.getMessage());
            }
        });
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private static boolean isNumeric(String str) {
        return str != null && str.matches("[0-9]+");
    }

    private String getHelp() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<gold>EnhancedOres Usage:</gold>\n")
                .append(" - /eo list - List all regions.\n")
                .append(" - /eo reload - Reload configuration.\n")
                .append(" - /eo region <green>[argument]</green> - Manage regions.\n")
                .append("   - <green><hover:show_text:'<yellow>/eo region check</yellow><gray> - Checks if region contain location.</gray>'>[check]</hover></green> - Hover me for more info.\n")
                .append("   - <green><hover:show_text:'<yellow>/eo region open [id/name]</yellow><gray> - Open/Create region session.</gray>'>[open] [id/name]</hover></green> - Hover me for more info.\n")
                .append("   - <green><hover:show_text:'<yellow>/eo region close</yellow><gray> - Close all player`s sessions.</gray>'>[close]</hover></green> - Hover me for more info.\n")
                .append("   - <green><hover:show_text:'<yellow>/eo region remove [id/name]</yellow><gray> - Remove region.</gray>'>[remove] [id/name]</hover></green> - Hover me for more info.");

        return stringBuilder.toString();
    }

    public boolean getDevmode() {
        return devmode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ec")) {

            if (args.length == 0) {
                Component help = MiniMessage.miniMessage().deserialize(getHelp());
                sender.sendMessage(help);
                return false;
            }

            if (!sender.isOp()) {
                Component message = MiniMessage.miniMessage().deserialize(translations.getString("EnhancedOres.messages.permissionDenied", "<error>'permissionDenied' not found!"),
                        Placeholder.styling("error", TextColor.fromHexString( Annotations.getTag("error") )),
                        Placeholder.styling("warning", TextColor.fromHexString( Annotations.getTag("warning") )),
                        Placeholder.styling("success", TextColor.fromHexString( Annotations.getTag("success") )),
                        Placeholder.styling("info", TextColor.fromHexString( Annotations.getTag("info") )));
                sender.sendMessage(message);
                return true;
            }

            switch (args[0].toLowerCase()) {

                case "devmode":

                    String dev;

                    if (devmode) {
                        devmode = false;
                        dev = "is now disabled.";
                    } else {
                        devmode = true;
                        dev = "is now enabled.";
                    }

                    Component status = MiniMessage.miniMessage().deserialize(("<gradient:#b28724:#ffc234>[Enhanced Ores]</gradient> <#ffe099>" + dev),
                            Placeholder.styling("error", TextColor.fromHexString( Annotations.getTag("error") )),
                            Placeholder.styling("warning", TextColor.fromHexString( Annotations.getTag("warning") )),
                            Placeholder.styling("success", TextColor.fromHexString( Annotations.getTag("success") )),
                            Placeholder.styling("info", TextColor.fromHexString( Annotations.getTag("info") )));

                    sender.sendMessage(status);

                    break;

                case "help":

                    Component help = MiniMessage.miniMessage().deserialize(getHelp());
                    sender.sendMessage(help);

                    break;

                case "reload":
                    reloadConfiguration();

                    Component reload = MiniMessage.miniMessage().deserialize(translations.getString("EnhancedOres.messages.configReloadSuccess", "<error>'configReloadSuccess' not found!"),
                            Placeholder.styling("error", TextColor.fromHexString( Annotations.getTag("error") )),
                            Placeholder.styling("warning", TextColor.fromHexString( Annotations.getTag("warning") )),
                            Placeholder.styling("success", TextColor.fromHexString( Annotations.getTag("success") )),
                            Placeholder.styling("info", TextColor.fromHexString( Annotations.getTag("info") )));
                    sender.sendMessage(reload);

                    break;

                default:
                    Component help2 = MiniMessage.miniMessage().deserialize(getHelp());
                    sender.sendMessage(help2);
                    return true;
            }
            return true;
        }

        return false;
    }

}
