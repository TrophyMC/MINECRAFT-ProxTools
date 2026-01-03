package de.mecrytv.proxyTools;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.mecrytv.proxyTools.manager.ServiceManager;
import de.mecrytv.proxyTools.utils.LogWithColor;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "proxytools", name = "ProxyTools", version = "1.0.0", authors = {"MecryTV"})
public class ProxyTools {

    private static ProxyTools instance;
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    private final ServiceManager serviceManager;

    @Inject
    public ProxyTools(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        instance = this;
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.serviceManager = new ServiceManager(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        startLog();

        serviceManager.initializeServices();
        serviceManager.registerAll();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        serviceManager.shutdownServices();
    }

    private void startLog() {
        String[] proxyToolsLogo = {
                "██████╗ ██████╗  ██████╗ ██╗  ██╗██╗   ██╗████████╗ ██████╗  ██████╗ ██╗     ███████╗",
                "██╔══██╗██╔══██╗██╔═══██╗╚██╗██╔╝╚██╗ ██╔╝╚══██╔══╝██╔═══██╗██╔═══██╗██║     ██╔════╝",
                "██████╔╝██████╔╝██║   ██║ ╚███╔╝  ╚████╔╝    ██║   ██║   ██║██║   ██║██║     ███████╗",
                "██╔═══╝ ██╔══██╗██║   ██║ ██╔██╗   ╚██╔╝     ██║   ██║   ██║██║   ██║██║     ╚════██║",
                "██║     ██║  ██║╚██████╔╝██╔╝ ██╗   ██║      ██║   ╚██████╔╝╚██████╔╝███████╗███████║",
                "╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝      ╚═╝    ╚═════╝  ╚═════╝ ╚══════╝╚══════╝",
                "                                                                                    ",
                "                                     ProxyTools                                     ",
                "                                Running on Velocity                                 "
        };
        for (String line: proxyToolsLogo) {
            logger.info(LogWithColor.color(line, LogWithColor.GREEN));
        }
        logger.info(LogWithColor.color("Developed by MecryTv", LogWithColor.GOLD));
        logger.info(LogWithColor.color("Plugin has been enabled!", LogWithColor.GREEN));
    }

    public static ProxyTools getInstance() { return instance; }
    public Logger getLogger() { return logger; }
    public ProxyServer getServer() { return server; }
    public Path getDataDirectory() { return dataDirectory; }
    public ServiceManager getServiceManager() { return serviceManager; }
}
