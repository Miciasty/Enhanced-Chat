package nsk.enhanced;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import nsk.enhanced.Events.Commands.AnnouncementsCommand;
import nsk.enhanced.Events.Commands.ClearChatCommand;
import nsk.enhanced.Events.Commands.PrivateMessageCommand;
import nsk.enhanced.Events.OnPlayerChat.LOW.OnPlayerChatEvent_LOW;
import nsk.enhanced.Events.OnPlayerChat.LOWEST.OnPlayerChatEvent_LOWEST;
import nsk.enhanced.Events.OnPlayerCommandPreprocess.OnPlayerCommandPreprocessLOWEST;
import nsk.enhanced.Player.Character;
import nsk.enhanced.System.EnhancedLogger;
import nsk.enhanced.System.HelpOp.Report;
import nsk.enhanced.System.HelpOp.ReportManager;
import nsk.enhanced.System.PluginInstance;
import nsk.enhanced.System.RunnableTask.AutoMessageTask;
import nsk.enhanced.Tags.Annotations;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerMoveEvent;
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
    private AutoMessageTask autoMessageTask;
    private ReportManager reportManager;

    private File configFile;
    private FileConfiguration config;

    private File translationsFile;
    private FileConfiguration translations;

    private File blacklistFile;
    private FileConfiguration blacklist;

    private File autoMessagesFile;
    private FileConfiguration autoMessages;

    private File announcementsFile;
    private FileConfiguration announcements;

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
        loadBlacklist();
        loadAutoMessages();
        loadAnnouncements();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            enhancedLogger.warning("Could not find PlaceholderAPI! This plugin is required.");
            getServer().getPluginManager().disablePlugin(this);
        }

        // --- --- --- --- --- --- --- --- --- --- //

        reportManager = new ReportManager();

        // --- --- --- --- --- --- --- --- --- --- //

        // configureHibernate();

        // --- --- --- --- // Events Managers & Listeners // --- --- --- --- //
        OnPlayerChatEvent_LOWEST onPlayerChatEvent_lowest = new OnPlayerChatEvent_LOWEST();
        OnPlayerChatEvent_LOW onPlayerChatEvent_low = new OnPlayerChatEvent_LOW();

        OnPlayerCommandPreprocessLOWEST onPlayerCommandPreprocessLOWEST = new OnPlayerCommandPreprocessLOWEST();

        try {
            getServer().getPluginManager().registerEvents(onPlayerChatEvent_lowest, this);
            getServer().getPluginManager().registerEvents(onPlayerChatEvent_low, this);

            enhancedLogger.fine("onPlayerChatEvent registered");
        } catch (Exception e) {
            enhancedLogger.severe("Registration onPlayerChatEvent failed! - " + e.getMessage());
        }

        try {
            getServer().getPluginManager().registerEvents(onPlayerCommandPreprocessLOWEST, this);

            enhancedLogger.fine("onPlayerCommandPreprocessEvent registered");
        } catch (Exception e) {
            enhancedLogger.severe("Registration onPlayerCommandPreprocessEvent failed! - " + e.getMessage());
        }

        // --- --- --- --- --- --- --- --- --- --- //

        PluginCommand ec = this.getCommand("ec");
        if (ec != null) {
            ec.setExecutor(this);
            enhancedLogger.fine("Command 'ec' registered.");
        } else {
            enhancedLogger.severe("Command 'ec' is not registered.");
        }

                PluginCommand msg = this.getCommand("msg");
                if (msg != null) {
                    msg.setExecutor(new PrivateMessageCommand());
                    enhancedLogger.fine("Command 'msg' registered.");
                } else {
                    enhancedLogger.severe("Command 'msg' is not registered.");
                }

                PluginCommand ignore = this.getCommand("ignore");
                if (ignore != null) {
                    ignore.setExecutor(new PrivateMessageCommand());
                    enhancedLogger.fine("Command 'ignore' registered.");
                } else {
                    enhancedLogger.severe("Command 'ignore' is not registered.");
                }

                PluginCommand clear = this.getCommand("chatclear");
                if (clear != null) {
                    clear.setExecutor(new ClearChatCommand());
                    enhancedLogger.fine("Command 'clear' registered.");
                } else {
                    enhancedLogger.severe("Command 'chatclear' is not registered.");
                }

                PluginCommand rules = this.getCommand("rules");
                if (rules != null) {
                    rules.setExecutor(this);
                    enhancedLogger.fine("Command 'rules' registered.");
                } else {
                    enhancedLogger.severe("Command 'rules' is not registered.");
                }

                PluginCommand chat = this.getCommand("chat");
                if (chat != null) {
                    chat.setExecutor(this);
                    enhancedLogger.fine("Command 'ooc' registered.");
                } else {
                    enhancedLogger.severe("Command 'chat' is not registered.");
                }

        // --- --- --- --- --- --- --- --- --- --- //

        PluginCommand report = this.getCommand("report");
        if (report != null) {
            report.setExecutor(this);
            enhancedLogger.fine("Command 'report' registered.");
        } else {
            enhancedLogger.severe("Command 'report' is not registered.");
        }

        PluginCommand helpop = this.getCommand("helpop");
        if (helpop != null) {
            helpop.setExecutor(this);
            enhancedLogger.fine("Command 'helpop' registered.");
        } else {
            enhancedLogger.severe("Command 'helpop' is not registered.");
        }

        // --- --- --- --- --- --- --- --- --- --- //

        PluginCommand announcement = this.getCommand("announcement");
        if (announcement != null) {
            announcement.setExecutor(new AnnouncementsCommand());
            enhancedLogger.fine("Command 'announcement' registered.");
        } else {
            enhancedLogger.severe("Command 'announcement' is not registered.");
        }

                PluginCommand warning = this.getCommand("warning");
                if (warning != null) {
                    warning.setExecutor(new AnnouncementsCommand());
                    enhancedLogger.fine("Command 'warning' registered.");
                } else {
                    enhancedLogger.severe("Command 'warning' is not registered.");
                }

                PluginCommand broadcast = this.getCommand("broadcast");
                if (broadcast != null) {
                    broadcast.setExecutor(new AnnouncementsCommand());
                    enhancedLogger.fine("Command 'broadcast' registered.");
                } else {
                    enhancedLogger.severe("Command 'broadcast' is not registered.");
                }

        // --- --- --- --- --- --- --- --- --- --- //

        getServer().getPluginManager().registerEvents(this, this);

        if (autoMessages.getBoolean("AutoMessages.enabled", true)) {
            int interval = autoMessages.getInt("AutoMessages.time", 300) * 20;

            getServer().getScheduler().runTaskTimer(this, new AutoMessageTask(this), interval, interval);
        }



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //


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
    public FileConfiguration getConfigFile() {
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

    private void loadBlacklist() {
        enhancedLogger.warning("Loading blacklist...");
        blacklistFile = new File(getDataFolder(), "blacklist.yml");
        if (!blacklistFile.exists()) {
            blacklistFile.getParentFile().mkdirs();
            saveResource("blacklist.yml", false);
        }

        blacklist = YamlConfiguration.loadConfiguration(blacklistFile);
    }
    public FileConfiguration getBlacklistFile() {
        return blacklist;
    }

    private void loadAutoMessages() {
        enhancedLogger.warning("Loading auto messages...");
        autoMessagesFile = new File(getDataFolder(), "auto_messages.yml");
        if (!autoMessagesFile.exists()) {
            autoMessagesFile.getParentFile().mkdirs();
            saveResource("auto_messages.yml", false);
        }

        autoMessages = YamlConfiguration.loadConfiguration(autoMessagesFile);
    }
    public FileConfiguration getAutoMessagesFile() {
        return autoMessages;
    }

    private void loadAnnouncements() {
        enhancedLogger.warning("Loading announcements...");
        announcementsFile = new File(getDataFolder(), "announcements.yml");
        if (!announcementsFile.exists()) {
            announcementsFile.getParentFile().mkdirs();
            saveResource("announcements.yml", false);
        }

        announcements = YamlConfiguration.loadConfiguration(announcementsFile);
    }
    public FileConfiguration getAnnouncementsFile() {
        return announcements;
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    private void reloadConfiguration() {
        try {

            loadTranslations();
            loadConfiguration();
            loadBlacklist();
            loadAutoMessages();
            loadAnnouncements();

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
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.hasChangedPosition()) {

            Player player = event.getPlayer();

            Character character = getCharacter(player.getUniqueId());
            if (character != null) {
                character.setBot(false);
            }

        }

    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- //

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        addCharacter( new Character(uuid,false));

        if (autoMessages.getBoolean("OnJoin.enabled")) {
            List<String> lines = autoMessages.getStringList("OnJoin.for_all_message" );

            for (String line : lines) {

                String text = PlaceholderAPI.setPlaceholders(player, line);
                Component l = MiniMessage.miniMessage().deserialize(text);

                event.joinMessage(l);
            }

            List<String> Plines = autoMessages.getStringList("OnJoin.for_player_message" );

            for (String pline : Plines) {
                String text = PlaceholderAPI.setPlaceholders(player, pline);
                Component l = MiniMessage.miniMessage().deserialize(text);

                player.sendMessage(l);
            }
        }
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

        if (autoMessages.getBoolean("OnDisconnect.enabled")) {
            List<String> lines = autoMessages.getStringList("OnDisconnect.for_all_message" );

            for (String line : lines) {

                String text = PlaceholderAPI.setPlaceholders(player, line);
                Component l = MiniMessage.miniMessage().deserialize(text);

                event.quitMessage(l);
            }
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

    public ReportManager getReportManager() {
        return reportManager;
    }

    private void changeChat(Player player, String type) {
        Character character = getCharacter(player.getUniqueId());

        if (character != null) {

            switch (type) {

                case "world":
                    character.setLocal(false);

                    String w = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                    Component w2 = MiniMessage.miniMessage().deserialize(w);
                    player.sendMessage(w2);
                    break;

                case "local":
                    character.setLocal(true);

                    String l = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                    Component l2 = MiniMessage.miniMessage().deserialize(l);
                    player.sendMessage(l2);
                    break;

                default:
                    if (character.isLocal()) {
                        character.setLocal(false);

                        String w3 = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                        Component w4 = MiniMessage.miniMessage().deserialize(w3);
                        player.sendMessage(w4);
                    } else {
                        String l3 = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                        Component l4 = MiniMessage.miniMessage().deserialize(l3);
                        player.sendMessage(l4);
                    }
            }
        }
    }

    private void changeChat(Player player) {
        Character character = getCharacter(player.getUniqueId());

        if (character != null) {
            if (character.isLocal()) {
                character.setLocal(false);

                String w3 = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                Component w4 = MiniMessage.miniMessage().deserialize(w3);
                player.sendMessage(w4);
            } else {
                String l3 = PlaceholderAPI.setPlaceholders(player, translations.getString("EnhancedOres.messages.ooc", "<green>[World chat] <gray>is now enabled."));
                Component l4 = MiniMessage.miniMessage().deserialize(l3);
                player.sendMessage(l4);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ec") || command.getName().equalsIgnoreCase("echat")) {

            if (args.length == 0) {
                Component help = MiniMessage.miniMessage().deserialize(getHelp());
                sender.sendMessage(help);
                return false;
            }

            if (!sender.isOp()) {
                Component message = MiniMessage.miniMessage().deserialize(translations.getString("EnhancedChat.messages.permissionDenied", "<error>'permissionDenied' not found!"),
                        Placeholder.styling("error", TextColor.fromHexString( Annotations.getTag("error") )),
                        Placeholder.styling("warning", TextColor.fromHexString( Annotations.getTag("warning") )),
                        Placeholder.styling("success", TextColor.fromHexString( Annotations.getTag("success") )),
                        Placeholder.styling("info", TextColor.fromHexString( Annotations.getTag("info") )));
                sender.sendMessage(message);
                return true;
            }

            switch (args[0].toLowerCase()) {

                case "character":

                    if (devmode) {

                        if (args.length == 2) {
                            Player player = getServer().getPlayer(args[1].toLowerCase());

                            if (player != null) {
                                Character character = getCharacter(player.getUniqueId());
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(character.toString()));
                            }
                        } else {

                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                Character character = getCharacter(player.getUniqueId());

                                player.sendMessage(MiniMessage.miniMessage().deserialize(character.toString()));
                            } else {
                                sender.sendMessage("You must be a player to use this command.");
                            }

                        }

                    }

                    break;

                case "devmode":

                    String dev;

                    if (devmode) {
                        devmode = false;
                        dev = "is now disabled.";
                    } else {
                        devmode = true;
                        dev = "is now enabled.";
                    }

                    Component status = MiniMessage.miniMessage().deserialize(("<gradient:#b28724:#ffc234>[Enhanced Chat]</gradient> <#ffe099>" + dev),
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

                    Component reload = MiniMessage.miniMessage().deserialize(translations.getString("EnhancedChat.messages.configReloadSuccess", "<error>'configReloadSuccess' not found!"),
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

        if (command.getName().equalsIgnoreCase("rules")){

            if (sender instanceof Player) {
                Player player = (Player) sender;

                List<String> lines = translations.getStringList("EnhancedChat.rules" );

                for (String line : lines) {

                    String text = PlaceholderAPI.setPlaceholders(player, line);
                    Component l = MiniMessage.miniMessage().deserialize(text);

                    player.sendMessage(l);
                }
            } else {
                enhancedLogger.info(sender + " just tried to use Player's command '/rules'");
            }

            return true;

        }

        if (command.getName().equalsIgnoreCase("chat")) {

            if (args.length == 0) {
                Component help = MiniMessage.miniMessage().deserialize(getHelp());
                sender.sendMessage(help);
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not a player."));
                return true;
            }

            Player player = (Player) sender;

            switch (args[0].toLowerCase()) {

                case "world":
                    changeChat(player, "world");
                    break;

                case "local":
                    changeChat(player, "local");
                    break;

                default:
                    changeChat(player);

            }

            return true;

        }

        if (command.getName().equalsIgnoreCase("report")) {

            if (args.length < 2) {
                return false;
            } else {

                String report = translations.getString("EnhancedChat.system.not_player");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Player suspect = Bukkit.getPlayer(args[0]);

                    if (suspect != null) {

                        StringBuilder sb = new StringBuilder();
                        for (int i=1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }

                        reportManager.addReport( new Report(player, suspect, sb.toString() ));
                        report = translations.getString("EnhancedChat.system.report_success");

                    } else {
                        report = translations.getString("EnhancedChat.system.report_suspect_not_found");
                    }
                }

                sender.sendMessage(MiniMessage.miniMessage().deserialize(report));

                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("helpop") ) {

            if (args.length < 1) {
                return false;
            } else if (!sender.isOp()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(translations.getString("EnhancedChat.system.permission")));
                return true;
            } else {

                switch (args[0].toLowerCase()) {

                    case "list":
                        String list = reportManager.viewReports();

                        sender.sendMessage(MiniMessage.miniMessage().deserialize(list));
                        break;

                    case "show":
                        if (args.length == 2 && isNumeric(args[1])) {
                            Report report = reportManager.getReport(Integer.parseInt(args[1]));

                            if (report != null) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(report.toString()));
                            } else {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Report with this ID wasn't found."));
                            }
                        }
                        break;

                    case "remove":
                        if (args.length == 2 && isNumeric(args[1])) {
                            Report report = reportManager.getReport(Integer.parseInt(args[1]));

                            if (report != null) {
                                reportManager.removeReport(report);
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Report was successfully removed."));
                            } else {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Report with this ID wasn't found."));
                            }
                        }
                        break;

                    default:
                        return false;

                }

                return  true;

            }

        }

        return false;
    }

    public EnhancedLogger getEnhancedLogger() {
        return enhancedLogger;
    }


}
