package de.mecrytv.proxyTools.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import de.mecrytv.proxyTools.ProxyTools;
import de.mecrytv.proxyTools.models.mariadb.AntiBotCache;
import de.mecrytv.proxyTools.models.redis.AntiBotModel;
import de.mecrytv.proxyTools.redis.RedisManager; // Dein Manager
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AntiBotListener {

    private static final String REDIS_ZAEHLER = "antibot:joins_sec";
    private static final String REDIS_SENTRY = "antibot:sentry_mode";

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        var sm = ProxyTools.getInstance().getServiceManager();
        if (!sm.getConfig().getBoolean("anti_bot.enabled")) {
            continuation.resume();
            return;
        }

        String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        AntiBotCache cache = sm.getCacheService().getNode("antibot_cache");
        RedisManager rm = sm.getRedisManager();

        try {
            long count = rm.incr(REDIS_ZAEHLER);
            if (count == 1) rm.expire(REDIS_ZAEHLER, 1);

            if (count > sm.getConfig().getInt("anti_bot.threshold")) {
                rm.setex(REDIS_SENTRY, sm.getConfig().getInt("anti_bot.sentry_duration"), "active");
            }

            if (rm.exists(REDIS_SENTRY)) {
                if (cache.get(ip) == null) {
                    String msg = sm.getMessages().getString("errors.anti_bot_kick");
                    event.setResult(LoginEvent.ComponentResult.denied(
                            MiniMessage.miniMessage().deserialize(msg.replace("\\n", "\n"))
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        continuation.resume();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        AntiBotCache cache = ProxyTools.getInstance().getServiceManager().getCacheService().getNode("antibot_cache");

        if (cache.get(ip) == null) {
            cache.set(new AntiBotModel(ip, System.currentTimeMillis()));
        }
    }
}