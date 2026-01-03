package de.mecrytv.proxyTools.manager;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.cache.CacheService;
import de.mecrytv.proxyTools.commands.MaintenanceCommand;
import de.mecrytv.proxyTools.commands.WhitelistCommand;
import de.mecrytv.proxyTools.listeners.*;
import de.mecrytv.proxyTools.mariadb.MariaDBManager;
import de.mecrytv.proxyTools.redis.RedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class ServiceManager {

    private final ProxyTools plugin;

    private ConfigManager config;
    private ConfigManager messages;
    private ConfigManager modt;
    private MariaDBManager mariaDBManager;
    private RedisManager redisManager;
    private CacheService cacheService;
    private Component prefix;

    public ServiceManager(ProxyTools plugin) {
        this.plugin = plugin;
    }

    public void initializeServices() {
        this.config = new ConfigManager(plugin.getDataDirectory(), "config.json");
        this.messages = new ConfigManager(plugin.getDataDirectory(), "messages/messages.json", "messages/messages.json");
        this.modt = new ConfigManager(plugin.getDataDirectory(), "messages/modt.json", "messages/modt.json");

        String prefixString = config.getString("prefix");
        this.prefix = MiniMessage.miniMessage().deserialize(prefixString);

        this.mariaDBManager = new MariaDBManager();
        this.redisManager = new RedisManager();
        this.cacheService = new CacheService();
        this.cacheService.initialize();
    }

    public void registerAll() {
        registerCommand(new WhitelistCommand(), "whitelist", "wl");
        registerCommand(new MaintenanceCommand(), "maintenance");

        registerListener(new ModtListener());
        registerListener(new ConnectionListener());
        registerListener(new CommandBlockListener());
        registerListener(new AnitVPNListener());
        registerListener(new AntiAltListener());
        registerListener(new AntiBotListener());

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (cacheService != null) cacheService.flushAll();
        }).repeat(1, TimeUnit.MINUTES).schedule();
    }

    public void shutdownServices() {
        Logger logger = plugin.getLogger();

        logger.info("");
        logger.info("  [ProxyTools] Shutting down services...");
        logger.info("  » Cache:   Flushing data...   [CLEANED]");
        if (cacheService != null) cacheService.flushAll();

        logger.info("  » Redis:   Disconnecting...   [OFFLINE]");
        if (redisManager != null) redisManager.disconnect();

        logger.info("  » MariaDB: Closing pool...    [CLOSED]");
        if (mariaDBManager != null) mariaDBManager.shutDown();

        logger.info("");
        logger.info("  ProxyTools has been disabled safely. Goodbye!");
        logger.info("");
    }

    public void reload() {
        config.reload();
        messages.reload();
        modt.reload();

        String prefixString = config.getString("prefix");
        this.prefix = MiniMessage.miniMessage().deserialize(prefixString);
    }

    private void registerCommand(SimpleCommand command, String label, String... aliases) {
        CommandManager commandManager = plugin.getServer().getCommandManager();
        commandManager.register(
                commandManager.metaBuilder(label)
                        .aliases(aliases)
                        .build(),
                command
        );
    }

    private void registerListener(Object listener) {
        plugin.getServer().getEventManager().register(plugin, listener);
    }

    public ConfigManager getMessages() {
        return messages;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public ConfigManager getModt() {
        return modt;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public MariaDBManager getMariaDBManager() {
        return mariaDBManager;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public Component getPrefix() {
        return prefix;
    }
}