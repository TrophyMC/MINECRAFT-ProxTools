package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.PlayerWhitelistCache;
import de.mecrytv.proxyTools.models.mariadb.ServerMaintenanceCache;
import de.mecrytv.proxyTools.models.redis.PlayerWhitelistModel;
import de.mecrytv.proxyTools.models.redis.ServerMaintenanceModel;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConnectionListener {

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        ServerMaintenanceCache serverCache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("server_maintenance_cache");
        ServerMaintenanceModel serverModel = serverCache.get("server_maintenance");

        PlayerWhitelistCache whitelistCache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("player_whitelist_cache");
        PlayerWhitelistModel whitelistModel = whitelistCache.get(player.getUniqueId().toString());

        if (serverModel != null && serverModel.isActive()) {
            boolean hasBypass = player.hasPermission("proxytools.maintenance.bypass");
            boolean isWhitelisted = (whitelistModel != null);

            if (!hasBypass && !isWhitelisted) {
                String kickMessage = ProxyTools.getInstance().getServiceManager().getMessages().getString("commands.maintenance.kick_message");

                event.setResult(ResultedEvent.ComponentResult.denied(
                        MiniMessage.miniMessage().deserialize(kickMessage)
                ));
            }
        }
    }
}
