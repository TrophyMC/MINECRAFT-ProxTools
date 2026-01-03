package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.AltAccountCache;
import de.mecrytv.proxyTools.models.redis.AltAccountModel;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;

public class AntiAltListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        var sm = ProxyTools.getInstance().getServiceManager();
        if (!sm.getConfig().getBoolean("anti_alt.enabled") || event.getPlayer().hasPermission("proxytools.antialt.bypass")) {
            continuation.resume();
            return;
        }

        String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        String uuid = event.getPlayer().getUniqueId().toString();
        AltAccountCache cache = sm.getCacheService().getNode("alt_account_cache");

        AltAccountModel model = cache.get(ip);
        if (model == null) {
            model = new AltAccountModel(ip, new ArrayList<>());
        }

        model.addUuid(uuid);
        cache.set(model);

        int maxAccounts = sm.getConfig().getInt("anti_alt.max_accounts_per_ip");
        if (model.getKnownUuids().size() > maxAccounts && !model.getKnownUuids().contains(uuid)) {
            String rawMsg = sm.getMessages().getString("errors.too_many_alts");
            event.setResult(LoginEvent.ComponentResult.denied(
                    MiniMessage.miniMessage().deserialize(rawMsg.replace("\\n", "\n"))
            ));
        }

        continuation.resume();
    }
}