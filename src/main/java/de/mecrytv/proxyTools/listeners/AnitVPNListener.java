package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.VPNCache;
import de.mecrytv.proxyTools.models.redis.VPNModel;
import de.mecrytv.proxyTools.utils.AntiVPNUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AnitVPNListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();

        if (player.hasPermission("proxytools.antivpn.bypass")) {
            continuation.resume();
            return;
        }

        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        VPNCache vpnCache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("vpn_cache");

        VPNModel cachedResult = vpnCache.get(ip);

        if (cachedResult != null) {
            if (cachedResult.isVpn()) {
                event.setResult(LoginEvent.ComponentResult.denied(getKickMessage()));
            }
            continuation.resume();
        } else {
            AntiVPNUtils.isProxy(ip).thenAccept(isVpn -> {
                try {
                    vpnCache.set(new VPNModel(ip, isVpn));

                    if (isVpn) {
                        event.setResult(LoginEvent.ComponentResult.denied(getKickMessage()));
                    }
                } finally {
                    continuation.resume();
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                continuation.resume();
                return null;
            });
        }
    }

    private Component getKickMessage() {
        String rawMsg = ProxyTools.getInstance().getServiceManager().getMessages().getString("errors.vpn_detected");
        return MiniMessage.miniMessage().deserialize(rawMsg.replace("\\n", "\n"));
    }
}
