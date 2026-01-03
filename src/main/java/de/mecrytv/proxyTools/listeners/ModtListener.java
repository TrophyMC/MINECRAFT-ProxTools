package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.mecrytv.proxyTools.ProxyTools;

public class ModtListener {

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        var sm = ProxyTools.getInstance().getServiceManager();
        var cache = sm.getCacheService().getNode("server_maintenance_cache");
        var model = (de.mecrytv.proxyTools.models.redis.ServerMaintenanceModel) cache.get("server_maintenance");

        ServerPing.Builder builder = event.getPing().asBuilder();
        var mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();

        String mode = (model != null && model.isActive()) ? "maintenance" : "default";

        String topLine = sm.getModt().getString(mode + ".top_line");
        String bottomLine = sm.getModt().getString(mode + ".bottom_line");

        builder.description(mm.deserialize(topLine + "\n" + bottomLine));

        event.setPing(builder.build());
    }
}